package net.nussi.dedicated_applied_energistics;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.modules.InfluxLogger;
import net.nussi.dedicated_applied_energistics.modules.VirtualInventory;
import net.nussi.dedicated_applied_energistics.providers.InterDimensionalInterfaceStorage;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = DedicatedAppliedEnegistics.MODID)
public class DedicatedAppliedEnergisticsController {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CONFIG_SPEC;

    public static ForgeConfigSpec.ConfigValue<String> CONFIG_VALUE_REDIS_URI;
    public static ForgeConfigSpec.ConfigValue<Boolean> CONFIG_VALUE_BEHAVIOUR_AUTOSTART;
    public static ForgeConfigSpec.ConfigValue<Boolean> CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY;
    public static ForgeConfigSpec.ConfigValue<Boolean> CONFIG_VALUE_BEHAVIOUR_INFLUXDB_LOGGER;

    static {
        CONFIG_BUILDER.push("Config for DAE2!");

        CONFIG_VALUE_REDIS_URI = CONFIG_BUILDER.define("REDIS_URI", "redis://localhost:6379");

        CONFIG_VALUE_BEHAVIOUR_AUTOSTART = CONFIG_BUILDER.define("BEHAVIOUR_AUTOSTART", false);
        CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY = CONFIG_BUILDER.define("BEHAVIOUR_VIRTUAL_INVENTORY", false);
        CONFIG_VALUE_BEHAVIOUR_INFLUXDB_LOGGER = CONFIG_BUILDER.define("BEHAVIOUR_INFLUXDB_LOGGER", false);

        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }


    public static boolean IsRunning = false;

    public static String Start() {
        if(IsRunning) return "DAE2 already started!";


        controllables.forEach(controllable -> {
            try {
                controllable.externalStart();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
        });
        if(CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.get()) VirtualInventory.Init();
        if(CONFIG_VALUE_BEHAVIOUR_INFLUXDB_LOGGER.get()) InfluxLogger.Init();

        IsRunning = true;
        return "OK";
    }

    public static String Stop() {
        IsRunning = false;

        controllables.forEach(controllable -> controllable.externalStop());
        VirtualInventory.Reset();
        InfluxLogger.Reset();
        if(jedisPool != null) jedisPool.close();

        return "OK";
    }

    public static String Reset() {
        String status = Stop();

        CONFIG_VALUE_REDIS_URI.set(CONFIG_VALUE_REDIS_URI.getDefault());

        CONFIG_VALUE_BEHAVIOUR_AUTOSTART.set(CONFIG_VALUE_BEHAVIOUR_AUTOSTART.getDefault());
        CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.set(CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.getDefault());

        return status;
    }

    public static JedisPool jedisPool;
    public static Jedis getJedis() {
        if(jedisPool == null) {
            jedisPool = new JedisPool(new JedisPoolConfig(), CONFIG_VALUE_REDIS_URI.get());
        }

        return jedisPool.getResource();
    }


    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) throws Exception {
        Stop();
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) throws Exception {
        if(CONFIG_VALUE_BEHAVIOUR_AUTOSTART.get()) Start();
    }

    public static List<Controllable> controllables = new ArrayList<>();
    public static void addControllable(Controllable controllable) {
        controllables.add(controllable);
    }

    public static abstract class Controllable {
        private final List<Jedis> jedisInstances = new ArrayList<>();
        private boolean running = false;

        public Controllable() {
            DedicatedAppliedEnergisticsController.addControllable(this);
            if(DedicatedAppliedEnergisticsController.IsRunning) this.externalStart();
        }

        protected abstract void onStart();
        protected abstract void onStop();

        protected Jedis getJedis() {
            Jedis jedis = DedicatedAppliedEnergisticsController.getJedis();
            jedisInstances.add(jedis);
            return jedis;
        }

        protected boolean isRunning() {
            return running;
        }

        public void externalStart() {
            this.onStart();
            this.running = true;
        }

        public void externalStop() {
            this.running = false;
            this.onStop();

            jedisInstances.forEach(Jedis::disconnect);
            jedisInstances.forEach(Jedis::close);
            jedisInstances.clear();
        }
    }

}
