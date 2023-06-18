package net.nussi.dedicated_applied_energistics.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import org.slf4j.Logger;
import redis.clients.jedis.util.JedisURIHelper;

import java.net.URI;
import java.net.URISyntaxException;

public class ConfigCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ConfigCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dae2")
                .then(Commands.literal("start").executes(context -> start(context.getSource())))
                .then(Commands.literal("stop").executes(context -> stop(context.getSource())))
                .then(Commands.literal("status").executes(context -> status(context.getSource())))
                .then(Commands.literal("restart").executes(context -> restart(context.getSource())))
                .then(Commands.literal("autostart")
                        .then(Commands.literal("enable").executes(context -> autostart(context.getSource(), true)))
                        .then(Commands.literal("disable").executes(context -> autostart(context.getSource(),false)))
                )
                .then(Commands.literal("virtual_inventory")
                        .then(Commands.literal("enable").executes(context -> virtualInventory(context.getSource(), true)))
                        .then(Commands.literal("disable").executes(context -> virtualInventory(context.getSource(),false)))
                )
                .then(Commands.literal("config")
                        .then(Commands.literal("set")
                                .then(Commands.argument("uri", StringArgumentType.string())
                                        .executes(context -> config(context.getSource(), StringArgumentType.getString(context,"uri")))))
                        .then(Commands.literal("reset").executes(context -> reset(context.getSource()))))
        );
    }

    private static int config(CommandSourceStack commandSourceStack, String uri) {
        try {
            if(!JedisURIHelper.isValid(new URI(uri))) {
                commandSourceStack.sendSystemMessage(Component.literal("Invalid redis uri!"));
                return 1;
            }
        } catch (URISyntaxException e) {
            commandSourceStack.sendSystemMessage(Component.literal("Invalid redis uri!"));
            return 1;
        }

        DedicatedAppliedEnergisticsController.CONFIG_VALUE_REDIS_URI.set(uri);
        commandSourceStack.sendSystemMessage(Component.literal("Set redis uri to: " + uri));
        return 1;
    }

    private static int status(CommandSourceStack commandSourceStack) {
        commandSourceStack.sendSystemMessage(Component.literal(
                "IsRunning: " + DedicatedAppliedEnergisticsController.IsRunning + "\n" +
                        "Redis URI: " + DedicatedAppliedEnergisticsController.CONFIG_VALUE_REDIS_URI.get() + "\n" +
                        "Autostart: " + DedicatedAppliedEnergisticsController.CONFIG_VALUE_BEHAVIOUR_AUTOSTART.get() + "\n" +
                        "VirtualInventory: " + DedicatedAppliedEnergisticsController.CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.get() + ""
        ));
        return 1;
    }

    private static int reset(CommandSourceStack commandSourceStack) {
        DedicatedAppliedEnergisticsController.Reset();
        commandSourceStack.sendSystemMessage(Component.literal("Reset config and stopped!"));
        return 1;
    }

    private static int start(CommandSourceStack commandSourceStack) {
        String status = DedicatedAppliedEnergisticsController.Start();
        commandSourceStack.sendSystemMessage(Component.literal(status));
        return 1;
    }

    private static int stop(CommandSourceStack commandSourceStack) {
        String status = DedicatedAppliedEnergisticsController.Stop();
        commandSourceStack.sendSystemMessage(Component.literal(status));
        return 1;
    }

    private static int restart(CommandSourceStack commandSourceStack) {
        stop(commandSourceStack);
        start(commandSourceStack);
        return 1;
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

    private static int virtualInventory(CommandSourceStack commandSourceStack, boolean enabled) {
        if(enabled == DedicatedAppliedEnergisticsController.CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.get()) {
            commandSourceStack.sendSystemMessage(Component.literal("Virtual inventory already " + enabled + "!"));
        } else {
            DedicatedAppliedEnergisticsController.CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.set(enabled);

            if(enabled) {
                DedicatedAppliedEnergisticsController.VirtualInventory.Init();
            } else {
                DedicatedAppliedEnergisticsController.VirtualInventory.Reset();
            }

            commandSourceStack.sendSystemMessage(Component.literal("Set virtual inventory to " + enabled + "!"));
        }
        return 1;
    }





}
