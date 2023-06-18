package net.nussi.dedicated_applied_energistics.modules;

import appeng.api.stacks.AEKey;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class VirtualInventory {
    public static Jedis outerJedis;
    public static Jedis innerJedis;
    public static JedisPubSub pubSub;
    public static Thread thread;

    public static void Init() {
        outerJedis = DedicatedAppliedEnergisticsController.getJedis();
        innerJedis = DedicatedAppliedEnergisticsController.getJedis();

        pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                try {
                    CompoundTag compoundTag = TagParser.parseTag(message);
                    String index = compoundTag.getString("Index");
                    compoundTag.remove("Index");

                    String UUID = compoundTag.getString("UUID");
                    compoundTag.remove("UUID");

                    CompoundTag item = compoundTag.getCompound("Item");
                    AEKey key = AEKey.fromTagGeneric(item);

                    long offsetAmount = compoundTag.getLong("Amount");
                    long newAmount = offsetAmount;
                    if(innerJedis.exists(index)) {
                        CompoundTag currentTag = TagParser.parseTag(innerJedis.get(index));
                        newAmount = currentTag.getLong("Amount");
                        if(newAmount < 0) newAmount = 0L;
                    }
                    compoundTag.putLong("Amount", newAmount);

                    if(newAmount > 0) innerJedis.set(index, compoundTag.getAsString());
                    else innerJedis.del(index);

                    InfluxLogger.OnVirtualInventory(key, index, offsetAmount, newAmount, UUID);

                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        thread = new Thread(() ->{
            outerJedis.subscribe(pubSub, "0.inv");
        });

        thread.start();
    }

    public static void Reset() {
        if(pubSub != null) {
            pubSub.unsubscribe("0.inv");
            pubSub.unsubscribe();
            pubSub = null;
        }

        if(innerJedis != null) {
            innerJedis.disconnect();
            innerJedis.close();
            innerJedis = null;
        }

        if(thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if(outerJedis != null) {
            outerJedis.disconnect();
            outerJedis.close();
            outerJedis = null;
        }
    }

}