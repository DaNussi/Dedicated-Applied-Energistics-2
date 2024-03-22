package net.nussi.dedicated_applied_energistics.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import org.slf4j.Logger;

public class StatusCommand implements Command {
    private static final Logger LOGGER = LogUtils.getLogger();

    public StatusCommand() {}

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> ConfigureCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder.then(Commands.literal("status").executes(context -> status(context.getSource())));
    }


    private static int status(CommandSourceStack commandSourceStack) {
        commandSourceStack.sendSystemMessage(Component.literal(
                        "Autostart: " + DedicatedAppliedEnergisticsController.CONFIG_VALUE_BEHAVIOUR_AUTOSTART.get() + ""
        ));
        return 1;
    }
}
