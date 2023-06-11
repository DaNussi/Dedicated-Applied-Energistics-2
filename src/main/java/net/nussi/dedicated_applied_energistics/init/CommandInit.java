package net.nussi.dedicated_applied_energistics.init;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics;
import net.nussi.dedicated_applied_energistics.commands.ConfigCommand;

@Mod.EventBusSubscriber(modid = DedicatedAppliedEnegistics.MODID)
public class CommandInit {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new ConfigCommand(event.getDispatcher());
    }

}
