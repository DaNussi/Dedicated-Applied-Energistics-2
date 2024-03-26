package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import com.mojang.logging.LogUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import net.minecraft.network.chat.Component;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import net.nussi.dedicated_applied_energistics.misc.RedisHelper;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.*;

public class VirtualDiskHost {
    private static final Logger LOGGER = LogUtils.getLogger();

    private InterDimensionalInterfaceBlockEntity instance;

    private Jedis redis;
    private Channel rabbitmq;
    private String channel;

    public MEStorage storage;
    private VirtualDiskInfo info;


    private final Queue<VirtualDiskAction.InsertPair> insertQueue = new LinkedList<>();

    public VirtualDiskHost(MEStorage storage, int priority, InterDimensionalInterfaceBlockEntity instance) {
        this.storage = storage;
        this.info = new VirtualDiskInfo(instance.getUuid(), priority);
        this.instance = instance;
        this.channel = RedisHelper.getPath(storage, instance);
    }

    public void onStart() {
        this.redis = instance.getRedis();
        this.rabbitmq = instance.getRabbitmq();

        this.initRabbitMQ();
        this.updateRedis();

        LOGGER.warn("Started virtual drive host " + channel);
    }

    public void onStop() {
        this.removeRedis();
        LOGGER.warn("Stopped virtual drive host " + channel);
    }

    private void initRabbitMQ() {
        try {
            rabbitmq.queueDeclare(channel + "/insert", false, false, false, null);
            rabbitmq.queueDeclare(channel + "/extract", false, false, false, null);
            rabbitmq.queueDeclare(channel + "/isPreferredStorageFor", false, false, false, null);
            rabbitmq.queueDeclare(channel + "/getAvailableStacks", false, false, false, null);
            rabbitmq.queueDeclare(channel + "/getDescription", false, false, false, null);


            DeliverCallback insertCallback = ((consumerTag, delivery) -> {
                var response = new VirtualDiskAction.InsertResponse(delivery.getProperties().getCorrelationId(), false, 0);
                try {
                    var request = new VirtualDiskAction.InsertRequest(delivery.getBody());
                    LOGGER.info("Incomming request " + request);
                    var pair = new VirtualDiskAction.InsertPair(request);
                    var callback = pair.getResponseFuture();
                    insertQueue.add(pair);
                    response = callback.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                LOGGER.info("Outgoin response " + response);

                AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder().correlationId(delivery.getProperties().getCorrelationId()).build();
                rabbitmq.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.toBytes());
                rabbitmq.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            });
            rabbitmq.basicConsume(channel + "/insert", false, insertCallback, (consumerTag -> {}));

        } catch (Exception e) {
            LOGGER.warn("Failed to declare rabbit mq queue for " + channel);
            e.printStackTrace();
        }
    }

    private void updateRedis() {
        this.redis.hset(VirtualDiskInfo.REDIS_PATH, channel, this.info.toString());
    }

    private void removeRedis() {
        this.redis.hdel(VirtualDiskInfo.REDIS_PATH, channel);
    }


    public void onTick() {
//        LOGGER.info("Ticking");

        while (insertQueue.size() > 0) {
            VirtualDiskAction.InsertPair pair = insertQueue.poll();
            VirtualDiskAction.InsertRequest request = pair.getRequest();
            long data = this.storage.insert(request.what, request.amount, request.mode, IActionSource.ofMachine(instance));
            VirtualDiskAction.InsertResponse response = new VirtualDiskAction.InsertResponse(request.getId(), true, data);
            pair.getResponseFuture().complete(response);
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VirtualDiskHost virtualDiskHost) {
            return virtualDiskHost.channel.equals(this.channel);
        }
        return false;
    }

    public String getChannel() {
        return channel;
    }


}
