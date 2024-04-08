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

    public static ForgeConfigSpec.ConfigValue<String> CONFIG_VALUE_REDIS_URI;

    public static ForgeConfigSpec.ConfigValue<Boolean> CONFIG_VALUE_BEHAVIOUR_AUTOSTART;

    static {
        CONFIG_BUILDER.push("Config for DAE2!");

        CONFIG_VALUE_REDIS_URI = CONFIG_BUILDER.define("REDIS_URI", "redis://localhost:6379/");

        CONFIG_VALUE_BEHAVIOUR_AUTOSTART = CONFIG_BUILDER.define("BEHAVIOUR_AUTOSTART", false);

        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
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
