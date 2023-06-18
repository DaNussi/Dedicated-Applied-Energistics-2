package net.nussi.dedicated_applied_energistics.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.slf4j.Logger;

public class GuiCommand implements Command {
    private static final Logger LOGGER = LogUtils.getLogger();

    public GuiCommand() {}

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> ConfigureCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder.then(Commands.literal("gui").executes(context -> gui(context.getSource())));
    }

    private static int gui(CommandSourceStack commandSourceStack) {
//        NetworkHooks.openScreen(commandSourceStack.getPlayer(), new SettingsMenuProvider(), commandSourceStack.getPlayer().getOnPos());
        return 1;
    }
}
