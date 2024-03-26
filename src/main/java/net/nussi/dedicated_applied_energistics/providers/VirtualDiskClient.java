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
            VirtualDiskAction.InsertRequest action = new VirtualDiskAction.InsertRequest(what, amount, mode);

            String replyQueueName = rabbitmq.queueDeclare().getQueue();
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(action.getId()).replyTo(replyQueueName).build();



            CompletableFuture<VirtualDiskAction.InsertResponse> responseFuture = new CompletableFuture<>();
            responseFuture.completeOnTimeout(new VirtualDiskAction.InsertResponse("",false,0), 5, TimeUnit.SECONDS);
            DeliverCallback insertCallback = ((consumerTag, delivery) -> {
                if(!delivery.getProperties().getCorrelationId().equals(action.getId())) return;
                VirtualDiskAction.InsertResponse response = new VirtualDiskAction.InsertResponse(action.getId(), false, 0);
                try {
                    response =  new VirtualDiskAction.InsertResponse(delivery.getBody());
                } catch (Exception e) {
                    LOGGER.error("Failed to pares response");
                    e.printStackTrace();
                }
                responseFuture.complete(response);
            });

            String ctag = rabbitmq.basicConsume(replyQueueName, true, insertCallback, consumerTag -> {});

            LOGGER.info("Calling insert action " + action);
            rabbitmq.basicPublish("", channel + "/insert", props, action.toBytes());

            VirtualDiskAction.InsertResponse response = responseFuture.get();
            rabbitmq.basicCancel(ctag);
            LOGGER.info("Callback for insert action " + response);
            if (response.isSuccess()) {
                return response.data;
            } else {
                LOGGER.error("Request was unsuccessful " + response);
                return 0;
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
