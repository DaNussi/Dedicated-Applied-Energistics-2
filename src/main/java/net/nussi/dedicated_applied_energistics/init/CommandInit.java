package net.nussi.dedicated_applied_energistics.init;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics;
import net.nussi.dedicated_applied_energistics.commands.*;

@Mod.EventBusSubscriber(modid = DedicatedAppliedEnegistics.MODID)
public class CommandInit {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("dae2");

        new GuiCommand().ConfigureCommand(builder);

        new StartCommand().ConfigureCommand(builder);
        new StopCommand().ConfigureCommand(builder);
        new StatusCommand().ConfigureCommand(builder);

        new RestartCommand().ConfigureCommand(builder);
        new AutostartCommand().ConfigureCommand(builder);

        new ConfigCommand().ConfigureCommand(builder);

        new InfluxCommand().ConfigureCommand(builder);
        new VirtualInventoryCommand().ConfigureCommand(builder);

        event.getDispatcher().register(builder);
    }

}
