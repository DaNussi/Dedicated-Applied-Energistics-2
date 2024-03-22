package net.nussi.dedicated_applied_energistics.init;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics;
import net.nussi.dedicated_applied_energistics.commands.AutostartCommand;
import net.nussi.dedicated_applied_energistics.commands.ConfigCommand;
import net.nussi.dedicated_applied_energistics.commands.StatusCommand;

@Mod.EventBusSubscriber(modid = DedicatedAppliedEnegistics.MODID)
public class CommandInit {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("dae2");

//        new GuiCommand().ConfigureCommand(builder);

        new StatusCommand().ConfigureCommand(builder);

        new AutostartCommand().ConfigureCommand(builder);

        new ConfigCommand().ConfigureCommand(builder);

        event.getDispatcher().register(builder);
    }

}
