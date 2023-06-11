package net.nussi.dedicated_applied_energistics.items;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.items.AEBaseItem;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import net.nussi.dedicated_applied_energistics.blockentities.TestBlockEntity;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Mod.EventBusSubscriber(modid = DedicatedAppliedEnegistics.MODID)
public class InterDimensionalStorageCell extends AEBaseItem implements StorageCell {
    private static final Logger LOGGER = LogUtils.getLogger();
    public InterDimensionalInterfaceBlockEntity blockEntity;
    public InterDimensionalStorageCell(Properties properties) {
        super(properties);

        redisInit();
    }

    @Override
    public CellState getStatus() {return CellState.EMPTY;}

    @Override
    public double getIdleDrain() {return 0;}

    @Override
    public void persist() {}


    static JedisPool jedisPool = new JedisPool();
    static Jedis jedis;
    static Jedis reciveJedis;
    static HashMap<AEKey, Long> localHashMap = new HashMap<>();
    static int inventoryIndex = 0;
    static String UUID = java.util.UUID.randomUUID().toString();
    static Thread thread;
    static JedisPubSub pubSub;

    public static void redisInit() {
        jedisPool = new JedisPool("localhost", 6379);
        jedis = jedisPool.getResource();
        reciveJedis = jedisPool.getResource();
//        LOGGER.info("Created 2 connections!");

        redisFetch();
        redisSubscriber();
    }

    public static void redisReset() {
        if(jedis != null) {
            jedis.disconnect();
            jedis.close();
            jedis = null;
        }

        if(pubSub != null) {
            pubSub.unsubscribe(redisChannel());
            pubSub.unsubscribe();
            pubSub = null;
        }

        if(reciveJedis != null) {
            reciveJedis.disconnect();
            reciveJedis.close();
            reciveJedis = null;
        }

        if(thread != null) {
            try {
                thread.join();
                thread = null;
            } catch (Exception e) {}
        }

        if(jedisPool != null) {
            jedisPool.close();
            jedisPool.destroy();
            jedisPool = null;
        }

//        LOGGER.info("Removed 2 connections!");
    }

    public static void redisFetch() {
        Set<String> keys = jedis.keys(redisChannel()+"/*.item");
        long maxCount = keys.size();
        int counter = 1;

        LOGGER.info("Fetching Inventory ...");
        for(String key : keys) {
            System.out.print("\rFetching Inventory [" + counter++ + "/" + maxCount + "]");

            try {
                String data = jedis.get(key);
                CompoundTag compoundTag = TagParser.parseTag(data);

                long amount = compoundTag.getLong("Amount");
                CompoundTag itemTag = compoundTag.getCompound("Item");
                AEKey what = AEKey.fromTagGeneric(itemTag);

                localHashMap.put(what, amount);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        System.out.println();
    }

    public static void redisSubscriber() {
        thread = new Thread(() -> {
            try {
                pubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        try {
                            CompoundTag compoundTag = TagParser.parseTag(message);
                            String msgUUID = compoundTag.getString("UUID");
                            if(msgUUID.equals(UUID)) return;

                            long amount = compoundTag.getLong("Amount");
                            CompoundTag itemTag = compoundTag.getCompound("Item");
                            AEKey what = AEKey.fromTagGeneric(itemTag);

                            offset(what, amount, Actionable.MODULATE, true);
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                };

                reciveJedis.subscribe(pubSub, redisChannel());
            } catch (Exception e) {}
        });
        thread.start();
    }

    public static String redisIndex(AEKey what) {
        return redisChannel() + "/" + itemKey(what) + ".item";
    }

    public static String redisChannel() {
        return inventoryIndex + ".inv";
    }

    public static String itemKey(AEKey what) {
        return String.valueOf(what.toTagGeneric().toString().hashCode());
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        offset(what, amount, mode, false);
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if(!localHashMap.containsKey(what)) return 0;

        long currentAmount = localHashMap.get(what);
        if (amount <= currentAmount) {
            offset(what, -amount, mode, false);
            return amount;
        } else {
            long newAmount = amount - (amount - currentAmount);
            offset(what, -newAmount, mode, false);
            return newAmount;
        }
    }

    public static void offset(AEKey what, long amount, Actionable mode, boolean fromMsg) {
        long newAmount = amount;

        if(localHashMap.containsKey(what)) {
            long currentAmount = localHashMap.get(what);
            newAmount += currentAmount;
//            LOGGER.info("CURRENT " + what.getDisplayName().toString() + " " + newAmount + " " + localHashMap.size());
        }

        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putLong("Amount", amount);
        compoundTag.put("Item", what.toTagGeneric());
        compoundTag.putString("UUID", UUID);
        compoundTag.putString("Index", redisIndex(what));

        if(mode != Actionable.SIMULATE) {
            if(!fromMsg) jedis.publish(redisChannel(), compoundTag.toString());

            if(newAmount > 0) {
                localHashMap.put(what, newAmount);
//                LOGGER.info("PUT " + what.getDisplayName().toString() + " " + newAmount + " " + localHashMap.size());
            } else {
                localHashMap.remove(what);
//                LOGGER.info("REMOVE " + what.getDisplayName().toString() + " " + 0 + " " + localHashMap.size());
            }
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for(Map.Entry<AEKey, Long> pair : localHashMap.entrySet()) {
            out.add(pair.getKey(), pair.getValue());
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        redisReset();
    }

}
