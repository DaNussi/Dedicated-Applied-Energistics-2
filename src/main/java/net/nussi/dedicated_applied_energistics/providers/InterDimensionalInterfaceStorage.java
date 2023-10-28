package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class InterDimensionalInterfaceStorage extends DedicatedAppliedEnergisticsController.Controllable implements IStorageProvider, MEStorage {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Jedis fetchJedis;
    private Jedis reciveJedis;
    private Hashtable<AEKey, Long> localHashMap = new Hashtable<>();
    private int inventoryIndex = 0;
    private String UUID = java.util.UUID.randomUUID().toString();
    private Thread thread;
    private JedisPubSub pubSub;
    private InterDimensionalInterfaceBlockEntity instance;

    public InterDimensionalInterfaceStorage(InterDimensionalInterfaceBlockEntity instance) {
        this.instance = instance;

        DedicatedAppliedEnergisticsController.addControllable(this);
        if(DedicatedAppliedEnergisticsController.IsRunning) this.externalStart();
    }

    @Override
    public void onStart() {
        fetchJedis = getJedis();
        reciveJedis = getJedis();

        redisFetch();
        redisSubscriber();
    }

    @Override
    public void onStop() {
        if(!localHashMap.isEmpty()) {
            localHashMap.clear();
        }

        if(pubSub != null) {
            pubSub.unsubscribe(redisChannel());
            pubSub.unsubscribe();
            pubSub = null;
        }

        if(thread != null) {
            try {
                thread.join();
                thread = null;
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void redisFetch() {
        Set<String> keys = fetchJedis.keys(redisChannel()+"/*.item");
        long maxCount = keys.size();
        int counter = 1;

        LOGGER.info("Fetching Inventory ...");
        for(String key : keys) {
            System.out.print("\rFetching Inventory [" + counter++ + "/" + maxCount + "]");

            try {
                String data = fetchJedis.get(key);
                CompoundTag compoundTag = TagParser.parseTag(data);

                long amount = compoundTag.getLong("Amount");
                CompoundTag itemTag = compoundTag.getCompound("Item");
                AEKey what = AEKey.fromTagGeneric(itemTag);

                localHashMap.put(what, amount);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    public void redisSubscriber() {
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

    public String redisIndex(AEKey what) {
        return redisChannel() + "/" + what.toTagGeneric().getString("id") + "/" + itemKey(what) + ".item";
    }

    public String redisChannel() {
        return inventoryIndex + ".inv";
    }

    public static String itemKey(AEKey what) {
        return String.valueOf(what.toTagGeneric().toString().hashCode());
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if(!isRunning()) {
            LOGGER.warn("Can't extract items because DAE2 is stopped!");
            return 0;
        }

        offset(what, amount, mode, false);
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if(!isRunning()) {
            LOGGER.warn("Can't extract items because DAE2 is stopped!");
            return 0;
        }
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

    public void offset(AEKey what, long amount, Actionable mode, boolean fromMsg) {
        if(!isRunning()) return;

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
            if(!fromMsg) fetchJedis.publish(redisChannel(), compoundTag.toString());

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
}
