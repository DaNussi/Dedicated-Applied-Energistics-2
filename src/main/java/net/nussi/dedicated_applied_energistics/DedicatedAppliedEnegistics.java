package net.nussi.dedicated_applied_energistics;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import net.nussi.dedicated_applied_energistics.init.BlockInit;
import net.nussi.dedicated_applied_energistics.init.ItemInit;
import net.nussi.dedicated_applied_energistics.init.TabInit;
import net.nussi.dedicated_applied_energistics.redsocket.Redsocket;
import org.slf4j.Logger;

import java.net.URISyntaxException;

@Mod(DedicatedAppliedEnegistics.MODID)
public class DedicatedAppliedEnegistics
{
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MODID = "dae2";
    public static Redsocket REDSOCKET;

    public DedicatedAppliedEnegistics()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ItemInit.ITEMS.register(modEventBus);
        BlockInit.BLOCKS.register(modEventBus);
        BlockEntityTypeInit.BLOCK_ENTITY_TYPES.register(modEventBus);
        TabInit.CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(DedicatedAppliedEnegistics::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DedicatedAppliedEnergisticsController.CONFIG_SPEC, "dae2-config.toml");

    }

    public static void commonSetup(FMLCommonSetupEvent event) {

        try {
            REDSOCKET = new Redsocket("dae2", DedicatedAppliedEnergisticsController.CONFIG_VALUE_REDIS_URI.get());
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to start redsocket");
            e.printStackTrace();
        }
    }

}
