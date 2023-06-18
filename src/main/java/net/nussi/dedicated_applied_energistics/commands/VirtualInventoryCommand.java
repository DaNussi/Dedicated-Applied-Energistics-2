package net.nussi.dedicated_applied_energistics.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import net.nussi.dedicated_applied_energistics.modules.VirtualInventory;
import org.slf4j.Logger;

public class VirtualInventoryCommand implements Command {
    private static final Logger LOGGER = LogUtils.getLogger();

    public VirtualInventoryCommand() {}

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> ConfigureCommand(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .then(Commands.literal("virtual_inventory")
                    .then(Commands.literal("enable").executes(context -> virtualInventory(context.getSource(), true)))
                    .then(Commands.literal("disable").executes(context -> virtualInventory(context.getSource(),false)))
        );
    }

    private static int virtualInventory(CommandSourceStack commandSourceStack, boolean enabled) {
        if(enabled == DedicatedAppliedEnergisticsController.CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.get()) {
            commandSourceStack.sendSystemMessage(Component.literal("Virtual inventory already " + enabled + "!"));
        } else {
            DedicatedAppliedEnergisticsController.CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.set(enabled);

            if(enabled) {
                VirtualInventory.Init();
            } else {
                VirtualInventory.Reset();
            }

            commandSourceStack.sendSystemMessage(Component.literal("Set virtual inventory to " + enabled + "!"));
        }
        return 1;
    }

}
