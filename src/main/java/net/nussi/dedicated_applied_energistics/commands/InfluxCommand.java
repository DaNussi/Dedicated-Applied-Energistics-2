package net.nussi.dedicated_applied_energistics.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import net.nussi.dedicated_applied_energistics.modules.InfluxLogger;
import org.slf4j.Logger;

public class InfluxCommand implements Command {
    private static final Logger LOGGER = LogUtils.getLogger();

    public InfluxCommand() {}

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> ConfigureCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .then(Commands.literal("influx_logger")
                        .then(Commands.literal("enable").executes(context -> influxLogger(context.getSource(), true)))
                        .then(Commands.literal("disable").executes(context -> influxLogger(context.getSource(),false)))
                );
    }

    private static int influxLogger(CommandSourceStack commandSourceStack, boolean enabled) {
        if(enabled == DedicatedAppliedEnergisticsController.CONFIG_VALUE_BEHAVIOUR_INFLUXDB_LOGGER.get()) {
            commandSourceStack.sendSystemMessage(Component.literal("Influx logger already " + enabled + "!"));
        } else {
            DedicatedAppliedEnergisticsController.CONFIG_VALUE_BEHAVIOUR_INFLUXDB_LOGGER.set(enabled);

            if(enabled) {
                InfluxLogger.Init();
            } else {
                InfluxLogger.Reset();
            }

            commandSourceStack.sendSystemMessage(Component.literal("Set influx logger to " + enabled + "!"));
        }
        return 1;
    }
}
