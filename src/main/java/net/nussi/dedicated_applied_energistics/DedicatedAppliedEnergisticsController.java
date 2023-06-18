package net.nussi.dedicated_applied_energistics;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.items.InterDimensionalStorageCell;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.util.JedisURIHelper;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = DedicatedAppliedEnegistics.MODID)
public class DedicatedAppliedEnergisticsController {


    public static ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CONFIG_SPEC;

    public static ForgeConfigSpec.ConfigValue<String> CONFIG_VALUE_REDIS_URI;
    public static ForgeConfigSpec.ConfigValue<Boolean> CONFIG_VALUE_BEHAVIOUR_AUTOSTART;
    public static ForgeConfigSpec.ConfigValue<Boolean> CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY;

    static {
        CONFIG_BUILDER.push("Config for DAE2!");

        CONFIG_VALUE_REDIS_URI = CONFIG_BUILDER.define("REDIS_URI", "localhost:6379");

        CONFIG_VALUE_BEHAVIOUR_AUTOSTART = CONFIG_BUILDER.define("BEHAVIOUR_AUTOSTART", false);
        CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY = CONFIG_BUILDER.define("BEHAVIOUR_VIRTUAL_INVENTORY", false);

        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }


    static JedisPool jedisPool = new JedisPool();
    public static boolean IsRunning = false;



    public static String Start() {
        if(IsRunning) return "DAE2 already started!";
        IsRunning = true;
        jedisPool = new JedisPool(CONFIG_VALUE_REDIS_URI.get());

        InterDimensionalStorageCell.redisInit();
        if(CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.get()) VirtualInventory.Init();

        return "OK";
    }

    public static String Stop() {
        IsRunning = false;

        InterDimensionalStorageCell.redisReset();
        VirtualInventory.Reset();

        if(jedisPool != null) {
            jedisPool.close();
            jedisPool.destroy();
            jedisPool = null;
        }

        return "OK";
    }

    public static String Reset() {
        String status = Stop();

        CONFIG_VALUE_REDIS_URI.set(CONFIG_VALUE_REDIS_URI.getDefault());

        CONFIG_VALUE_BEHAVIOUR_AUTOSTART.set(CONFIG_VALUE_BEHAVIOUR_AUTOSTART.getDefault());
        CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.set(CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.getDefault());

        return status;
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }


    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) throws Exception {
        Stop();
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) throws IOException {
        if(CONFIG_VALUE_BEHAVIOUR_AUTOSTART.get()) Start();
    }

    public static class VirtualInventory {
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

                        Long newAmount = compoundTag.getLong("Amount");
                        if(innerJedis.exists(index)) {
                            CompoundTag currentTag = TagParser.parseTag(innerJedis.get(index));
                            newAmount += currentTag.getLong("Amount");
                        }
                        compoundTag.putLong("Amount", newAmount);

                        if(newAmount > 0) innerJedis.set(index, compoundTag.getAsString());
                        else innerJedis.del(index);
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

}
