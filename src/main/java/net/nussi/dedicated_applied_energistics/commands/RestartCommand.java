package net.nussi.dedicated_applied_energistics.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import org.slf4j.Logger;

public class RestartCommand implements Command {
    private static final Logger LOGGER = LogUtils.getLogger();

    public RestartCommand() {}

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> ConfigureCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder.then(Commands.literal("restart").executes(context -> restart(context.getSource())));
    }

    private static int restart(CommandSourceStack commandSourceStack) {
        String status;
        status = DedicatedAppliedEnergisticsController.Stop();
        commandSourceStack.sendSystemMessage(Component.literal(status));
        status = DedicatedAppliedEnergisticsController.Start();
        commandSourceStack.sendSystemMessage(Component.literal(status));
        return 1;
    }
}
