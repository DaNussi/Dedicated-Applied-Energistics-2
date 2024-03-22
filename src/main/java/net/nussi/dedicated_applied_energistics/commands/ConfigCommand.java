package net.nussi.dedicated_applied_energistics.commands;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public class ConfigCommand implements Command {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ConfigCommand() {}

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> ConfigureCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .then(Commands.literal("config")
                        .then(Commands.literal("set")
                                .then(Commands.argument("uri", StringArgumentType.string())
                                        .executes(context -> config(context.getSource(), StringArgumentType.getString(context,"uri")))))
                        .then(Commands.literal("reset").executes(context -> reset(context.getSource()))));
    }

    private static int config(CommandSourceStack commandSourceStack, String uri) {
        commandSourceStack.sendSystemMessage(Component.literal("Set redis uri to: " + uri));
        return 1;
    }

    private static int reset(CommandSourceStack commandSourceStack) {
        commandSourceStack.sendSystemMessage(Component.literal("Reset config and stopped!"));
        return 1;
    }

}
