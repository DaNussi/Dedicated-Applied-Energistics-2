package net.nussi.dedicated_applied_energistics.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import org.slf4j.Logger;

public class StopCommand implements Command {
    private static final Logger LOGGER = LogUtils.getLogger();

    public StopCommand() {}

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> ConfigureCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder.then(Commands.literal("stop").executes(context -> stop(context.getSource())));
    }

    private static int stop(CommandSourceStack commandSourceStack) {
        String status = DedicatedAppliedEnergisticsController.Stop();
        commandSourceStack.sendSystemMessage(Component.literal(status));
        return 1;
    }
}
