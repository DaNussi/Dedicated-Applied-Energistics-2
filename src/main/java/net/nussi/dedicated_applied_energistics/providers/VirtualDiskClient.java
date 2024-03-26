package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.mojang.logging.LogUtils;
import com.rabbitmq.client.*;
import net.minecraft.network.chat.Component;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class VirtualDiskClient implements MEStorage {
    private static final Logger LOGGER = LogUtils.getLogger();

    private InterDimensionalInterfaceBlockEntity instance;
    private String channel;
    private Jedis redis;
    private Channel rabbitmq;

    private VirtualDiskInfo info;

    public VirtualDiskClient(String channel, InterDimensionalInterfaceBlockEntity instance) {
        this.channel = channel;
        this.instance = instance;
    }

    public void onStart() {
        this.redis = instance.getRedis();
        this.rabbitmq = instance.getRabbitmq();

        boolean initRedisSuccess = initRedis();
        if (!initRedisSuccess) {
            onStop();
            return;
        }

        boolean initRabbitMqSuccess = initRabbitmq();
        if (!initRabbitMqSuccess) {
            onStop();
            return;
        }

        LOGGER.warn("Started virtual drive client " + channel);
    }

    public void onStop() {

        LOGGER.warn("Stopped virtual drive client " + channel);
    }

    private boolean initRedis() {
        try {
            String data = this.redis.hget(VirtualDiskInfo.REDIS_PATH, channel);
            this.info = VirtualDiskInfo.fromString(data);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to fetch VirtualDiskClient info for " + channel);
            return false;
        }
    }

    private boolean initRabbitmq() {
        return true;
    }


    public String getChannel() {
        return channel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VirtualDiskClient virtualDiskClient) {
            return virtualDiskClient.channel.equals(this.channel);
        }
        return false;
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return false;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        try {
            String corrId = UUID.randomUUID().toString();
            String replyQueueName = rabbitmq.queueDeclare().getQueue();
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName).build();



            CompletableFuture<VirtualDiskAction.InsertResponse> responseFuture = new CompletableFuture<>();
            responseFuture.completeOnTimeout(new VirtualDiskAction.InsertResponse("",false,0), 2, TimeUnit.SECONDS);
            DeliverCallback insertCallback = ((consumerTag, delivery) -> {
                if(!delivery.getProperties().getCorrelationId().equals(corrId)) return;
                VirtualDiskAction.InsertResponse response = new VirtualDiskAction.InsertResponse(delivery.getBody());
                LOGGER.info("Callback for insert action " + response);
                responseFuture.complete(response);
            });

            String ctag = rabbitmq.basicConsume(replyQueueName, true, insertCallback, consumerTag -> {});

            VirtualDiskAction.Insert action = new VirtualDiskAction.Insert(what, amount, mode);
            LOGGER.info("Calling insert action " + action);
            rabbitmq.basicPublish("", channel + "/insert", props, action.getData());

            VirtualDiskAction.InsertResponse response = responseFuture.get();
            rabbitmq.basicCancel(ctag);
            if (response.success) {
                return response.data;
            } else {
                throw new Exception("Request not successful");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {

    }

    @Override
    public Component getDescription() {
        return Component.literal("This is a great description!");
    }
}
