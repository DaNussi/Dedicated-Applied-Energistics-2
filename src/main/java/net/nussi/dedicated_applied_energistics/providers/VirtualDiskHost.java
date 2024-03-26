package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.definitions.AEItems;
import appeng.init.InitItems;
import com.mojang.logging.LogUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import net.nussi.dedicated_applied_energistics.misc.RedisHelper;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

public class VirtualDiskHost {
    private static final Logger LOGGER = LogUtils.getLogger();

    private InterDimensionalInterfaceBlockEntity instance;

    private Jedis redis;
    private Channel rabbitmq;
    private String channel;

    private MEStorage storage;
    private VirtualDiskInfo info;

    public VirtualDiskHost(MEStorage storage, int priority, InterDimensionalInterfaceBlockEntity instance) {
        this.storage = storage;
        this.storage.insert(AEItemKey.of(AEItems.ITEM_CELL_1K.stack()), 0, Actionable.MODULATE, IActionSource.ofMachine(instance));
        this.info = new VirtualDiskInfo(instance.getUuid(), priority);
        this.instance = instance;
        this.channel = RedisHelper.getPath(storage, instance);
    }

    public void onStart() {
        this.redis = instance.getRedis();
        this.rabbitmq = instance.getRabbitmq();

        this.initRabbitMQ();
        this.updateRedis();
        this.storage.insert(AEItemKey.of(AEItems.ITEM_CELL_256K.stack()), 0, Actionable.MODULATE, IActionSource.ofMachine(instance));

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
                VirtualDiskAction.InsertResponse response = insert(delivery.getBody());
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder().correlationId(delivery.getProperties().getCorrelationId()).build();
                rabbitmq.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getData());
                rabbitmq.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            });
            rabbitmq.basicConsume(channel + "/insert", false, insertCallback, (consumerTag -> {}));


        } catch (Exception e) {
            LOGGER.error("Failed to declare rabbit mq queue for " + channel);
            e.printStackTrace();
        }
    }

    private void updateRedis() {
        this.redis.hset(VirtualDiskInfo.REDIS_PATH, channel, this.info.toString());
    }

    private void removeRedis() {
        this.redis.hdel(VirtualDiskInfo.REDIS_PATH, channel);
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


    public boolean isPreferredStorageFor(byte[] bytes) {

        return false;
    }


    public VirtualDiskAction.InsertResponse insert(byte[] bytes) {
        VirtualDiskAction.Insert insertAction = new VirtualDiskAction.Insert(bytes);
        LOGGER.info("Received insert action " + insertAction);
        long data = storage.insert(insertAction.what, insertAction.amount, insertAction.mode, IActionSource.ofMachine(instance));
        VirtualDiskAction.InsertResponse response = new VirtualDiskAction.InsertResponse(insertAction.id, true, data);
        LOGGER.info("Responding insert action " + response);
        return response;
    }


    public long extract(byte[] bytes) throws Exception {
        VirtualDiskAction.Extract extractAction = new VirtualDiskAction.Extract(bytes);
        return storage.insert(extractAction.what, extractAction.amount, extractAction.mode, IActionSource.ofMachine(instance));
    }


    public void getAvailableStacks(byte[] bytes) {

    }


    public Component getDescription(byte[] bytes) {


        return Component.literal("This is a great description!");
    }
}
