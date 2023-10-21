package net.nussi.dedicated_applied_energistics.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.nussi.dedicated_applied_energistics.menus.SettingsScreen;
import org.slf4j.Logger;

public class GuiCommand implements Command {
    private static final Logger LOGGER = LogUtils.getLogger();

    public GuiCommand() {}

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> ConfigureCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder.then(Commands.literal("gui").executes(context -> gui(context.getSource())));
    }

    private static int gui(CommandSourceStack commandSourceStack) {
        try { Minecraft.getInstance().setScreen(null); } catch (Exception e) { }
        Minecraft.getInstance().setScreen(new SettingsScreen());

        /*
        commandSourceStack.getPlayer().openMenu(new SimpleMenuProvider(
                (worldId, inventory, player) -> new SettingsContainer(),
                Component.literal("Settings")
        ));
         */
        return 1;
    }
}
