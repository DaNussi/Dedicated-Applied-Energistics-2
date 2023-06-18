package net.nussi.dedicated_applied_energistics;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.items.InterDimensionalStorageCell;
import net.nussi.dedicated_applied_energistics.modules.InfluxLogger;
import net.nussi.dedicated_applied_energistics.modules.VirtualInventory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = DedicatedAppliedEnegistics.MODID)
public class DedicatedAppliedEnergisticsController {


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

        InterDimensionalStorageCell.redisInit();
        if(CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.get()) VirtualInventory.Init();
        if(CONFIG_VALUE_BEHAVIOUR_INFLUXDB_LOGGER.get()) InfluxLogger.Init();

        IsRunning = true;
        return "OK";
    }

    public static String Stop() {
        IsRunning = false;

        InterDimensionalStorageCell.redisReset();
        VirtualInventory.Reset();
        InfluxLogger.Reset();

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
        return new Jedis(CONFIG_VALUE_REDIS_URI.get());
    }


    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) throws Exception {
        Stop();
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) throws IOException {
        if(CONFIG_VALUE_BEHAVIOUR_AUTOSTART.get()) Start();
    }


}
