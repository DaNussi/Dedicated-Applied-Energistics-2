package net.nussi.dedicated_applied_energistics.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import org.slf4j.Logger;

public class AutostartCommand implements Command {
    private static final Logger LOGGER = LogUtils.getLogger();

    public AutostartCommand() {}

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> ConfigureCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .then(Commands.literal("autostart")
                    .then(Commands.literal("enable").executes(context -> autostart(context.getSource(), true)))
                    .then(Commands.literal("disable").executes(context -> autostart(context.getSource(),false)))
        );
    }

    private static int autostart(CommandSourceStack commandSourceStack, boolean enabled) {
        if(enabled == DedicatedAppliedEnergisticsController.CONFIG_VALUE_BEHAVIOUR_AUTOSTART.get()) {
            commandSourceStack.sendSystemMessage(Component.literal("Autostart already " + enabled + "!"));
        } else {
            DedicatedAppliedEnergisticsController.CONFIG_VALUE_BEHAVIOUR_AUTOSTART.set(enabled);
            commandSourceStack.sendSystemMessage(Component.literal("Set autostart to " + enabled + "!"));
        }
        return 1;
    }
}
