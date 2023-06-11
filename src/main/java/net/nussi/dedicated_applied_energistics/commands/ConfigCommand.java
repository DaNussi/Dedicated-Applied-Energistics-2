package net.nussi.dedicated_applied_energistics.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.core.tools.BasicCommandLineArguments;
import org.slf4j.Logger;

public class ConfigCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ConfigCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dae2")
                .then(Commands.literal("configure")
                        .then(Commands.argument("host", StringArgumentType.word())
                                .then(Commands.argument("port", IntegerArgumentType.integer()).executes(context -> configure(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "host"),
                                        IntegerArgumentType.getInteger(context, "port")
                                )))))
                .then(Commands.literal("status").executes(context -> status(context.getSource())))
                .then(Commands.literal("apply").executes(context -> apply(context.getSource())))
                .then(Commands.literal("reset").executes(context -> reset(context.getSource())))
        );
    }

    private int configure(CommandSourceStack commandSourceStack, String host, int port) {

        StringBuilder builder = new StringBuilder();
        builder.append("Configure");
        builder.append("  Host: ").append(host);
        builder.append("  Port: ").append(port);

        LOGGER.info(builder.toString());
        commandSourceStack.sendSystemMessage(Component.literal(builder.toString()));
        return 1;
    }

    private int status(CommandSourceStack commandSourceStack) {

        return 1;
    }

    private int apply(CommandSourceStack commandSourceStack) {

        return 1;
    }

    private int reset(CommandSourceStack commandSourceStack) {

        return 1;
    }

}
