package net.nussi.dedicated_applied_energistics;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = DedicatedAppliedEnegistics.MODID)
public class DedicatedAppliedEnergisticsController {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CONFIG_SPEC;

    public static ForgeConfigSpec.ConfigValue<String> CONFIG_VALUE_RABBITMQ_URI;
    public static ForgeConfigSpec.ConfigValue<String> CONFIG_VALUE_REDIS_URI;
    public static ForgeConfigSpec.ConfigValue<String> CONFIG_VALUE_HOST_ID;
    public static ForgeConfigSpec.ConfigValue<String> CONFIG_VALUE_HOST_IP;
    public static ForgeConfigSpec.ConfigValue<Integer> CONFIG_VALUE_HOST_PORT;

    public static ForgeConfigSpec.ConfigValue<Boolean> CONFIG_VALUE_BEHAVIOUR_AUTOSTART;
    public static ForgeConfigSpec.ConfigValue<Boolean> CONFIG_VALUE_BEHAVIOUR_HOST_IP_AUTO_DETECT;

    static {
        CONFIG_BUILDER.push("Config for DAE2!");

        CONFIG_VALUE_RABBITMQ_URI = CONFIG_BUILDER.define("RABBITMQ_URI", "amqp://guest:guest@localhost:5672/");
        CONFIG_VALUE_REDIS_URI = CONFIG_BUILDER.define("REDIS_URI", "redis://localhost:6379/");
        CONFIG_VALUE_HOST_ID = CONFIG_BUILDER.define("HOST_ID", UUID.randomUUID().toString());
        CONFIG_VALUE_HOST_IP = CONFIG_BUILDER.define("HOST_IP", getHostAddress());
        CONFIG_VALUE_HOST_PORT = CONFIG_BUILDER.define("HOST_PORT", 7894);

        CONFIG_VALUE_BEHAVIOUR_AUTOSTART = CONFIG_BUILDER.define("BEHAVIOUR_AUTOSTART", false);
        CONFIG_VALUE_BEHAVIOUR_HOST_IP_AUTO_DETECT = CONFIG_BUILDER.define("BEHAVIOUR_HOST_IP_AUTO_DETECT", true);

        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }

    public static String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            LOGGER.error("Failed to get host ip using 127.0.0.1");
            return "127.0.0.1";
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) throws Exception {
//        Stop();
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) throws Exception {
//        if(CONFIG_VALUE_BEHAVIOUR_AUTOSTART.get()) Start();
    }

}
