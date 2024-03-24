package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import com.mojang.logging.LogUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Map;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController.*;

public class InterDimensionalInterfaceStorage implements IStorageProvider, MEStorage {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Hashtable<AEKey, Long> localHashMap = new Hashtable<>();
    private String uuid = java.util.UUID.randomUUID().toString();
    private InterDimensionalInterfaceBlockEntity instance;

    private Connection rabbit_connection;
    private Channel rabbitmq_channel;
    private JedisPool redis_pool;
    private Jedis redis_connection;


    public InterDimensionalInterfaceStorage(InterDimensionalInterfaceBlockEntity instance) {
        this.instance = instance;
    }

    public void onStart() {
        LOGGER.info(uuid + " | Starting storage provider");
        boolean success = connectDatabase();
        if(!success) return;


        LOGGER.info(uuid + " | Started storage provider");
    }

    public void onStop() {
        LOGGER.info(uuid + " | Stopping storage provider");
        disconnectDatabase();
        LOGGER.info(uuid + " | Stopped storage provider");
    }

    // Returns true on success!
    private boolean connectDatabase() {

        for (int i = 0; i <= 5; i++) {
            try {
                LOGGER.debug(uuid + " | Connecting to " + CONFIG_VALUE_RABBITMQ_URI.get());
                ConnectionFactory factory = new ConnectionFactory();
                factory.setUri(CONFIG_VALUE_RABBITMQ_URI.get());


                this.rabbit_connection = factory.newConnection();
                this.rabbitmq_channel = rabbit_connection.createChannel();
                this.rabbitmq_channel.queueDeclare("ITEM_QUEUE", true, false, false, null);

                break;
            } catch (Exception e) {
                LOGGER.error(uuid + " | Failed to start rabbit mq connection! Retry " + i + "/5... ");
                e.printStackTrace();
                if(i == 5) return false;
            }
        }

        for (int i = 0; i <= 5; i++) {
            try {
                LOGGER.debug(uuid + " | Connecting to " + CONFIG_VALUE_REDIS_URI.get());
                redis_pool = new JedisPool(new URI(CONFIG_VALUE_REDIS_URI.get()));
                redis_connection = redis_pool.getResource();
            } catch (Exception e) {
                LOGGER.error(uuid + " | Failed to start redis connection! Retry " + i + "/5... ");
                e.printStackTrace();
                if(i == 5) return false;
            }
        }
        return true;
    }

    private void disconnectDatabase() {

        if(this.rabbitmq_channel != null && this.rabbitmq_channel.isOpen()) {
            try {
                this.rabbitmq_channel.close();
            } catch (Exception e) {
                LOGGER.error(uuid + " | Failed to close rabbit mq channel");
                e.printStackTrace();
            }
        }

        if(this.rabbit_connection != null && this.rabbit_connection.isOpen()) {
            try {
                this.rabbit_connection.close();
            } catch (Exception e) {
                LOGGER.error(uuid + " | Failed to close rabbit mq connection");
                e.printStackTrace();
            }
        }

        if(this.redis_connection != null && this.redis_connection.isConnected()) {
            try {
                this.redis_connection.close();
            } catch (Exception e) {
                LOGGER.error(uuid + " | Failed to close redis connection");
                e.printStackTrace();
            }
        }


        if(this.redis_pool != null && !this.redis_pool.isClosed()) {
            try {
                this.redis_pool.close();
            } catch (Exception e) {
                LOGGER.error(uuid + " | Failed to close redis pool");
                e.printStackTrace();
            }
        }
    }



    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        try {
            LOGGER.info("Found machine classes");
            for(var s : this.instance.getMainNode().getGrid().getMachineClasses()) {
                LOGGER.info("Class: " + s.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        StorageAction action = new StorageAction(what, amount, mode, this.uuid, StorageActionType.INSERT);

        try {
            LOGGER.info(uuid + " | Sending storage insert action");
            rabbitmq_channel.basicPublish("","ITEM_QUEUE", null, action.getData());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        StorageAction action = new StorageAction(what, amount, mode, this.uuid, StorageActionType.EXTRACT);

        try {
            LOGGER.info(uuid + " | Sending storage extract action");
            rabbitmq_channel.basicPublish("","ITEM_QUEUE", null, action.getData());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        Hashtable<AEKey, Long> temp = (Hashtable<AEKey, Long>) localHashMap.clone();

        for(Map.Entry<AEKey, Long>  pair : temp.entrySet()) {
            out.add(pair.getKey(), pair.getValue());
        }
    }

    @Override
    public Component getDescription() {
        return Component.literal("Inter Dimensional Interface Storage");
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        storageMounts.mount(this);
    }

    public static class StorageAction implements Serializable {
        AEKey what;
        long amount;
        Actionable mode;
        String sender;
        StorageActionType type;

        public StorageAction(AEKey what, long amount, Actionable mode, String sender, StorageActionType type) {
            this.what = what;
            this.amount = amount;
            this.mode = mode;
            this.sender = sender;
            this.type = type;
        }

        public byte[] getData() {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putLong("Amount", amount);
            compoundTag.put("Item", what.toTagGeneric());
            compoundTag.putBoolean("Simulate", mode == Actionable.SIMULATE);
            compoundTag.putString("Sender", sender);
            compoundTag.putString("Type", type.toString());

            return compoundTag.toString().getBytes(StandardCharsets.UTF_8);
        }

        public static StorageAction fromData(byte[] data) throws Exception {
            String stringData = new String(data, StandardCharsets.UTF_8);
            CompoundTag compoundTag = TagParser.parseTag(stringData);

            long amount = compoundTag.getLong("Amount");

            CompoundTag itemTag = compoundTag.getCompound("Item");
            AEKey what = AEKey.fromTagGeneric(itemTag);

            Actionable mode = compoundTag.getBoolean("Simulate") ? Actionable.SIMULATE : Actionable.MODULATE;

            String sender = compoundTag.getString("Sender");

            StorageActionType type = StorageActionType.valueOf(compoundTag.getString("Type"));

            return new StorageAction(what,amount,mode,sender, type);
        }
    }

    public enum StorageActionType {
        INSERT,
        EXTRACT
    }

}
