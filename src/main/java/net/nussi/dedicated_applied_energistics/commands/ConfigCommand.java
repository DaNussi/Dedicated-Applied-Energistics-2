package net.nussi.dedicated_applied_energistics.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics;
import net.nussi.dedicated_applied_energistics.items.InterDimensionalStorageCell;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = DedicatedAppliedEnegistics.MODID)
public class ConfigCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static Config currentConfig = Config.getDefault();
    public static Config pendingConfig = Config.getDefault();
    public static boolean IsRunning = false;
    public static boolean IsApplied = true;


    public static ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CONFIG_SPEC;

    public static ForgeConfigSpec.ConfigValue<String> CONFIG_VALUE_REDIS_HOST;
    public static ForgeConfigSpec.ConfigValue<Integer> CONFIG_VALUE_REDIS_PORT;
    public static ForgeConfigSpec.ConfigValue<String> CONFIG_VALUE_REDIS_USERNAME;
    public static ForgeConfigSpec.ConfigValue<String> CONFIG_VALUE_REDIS_PASSWORD;
    public static ForgeConfigSpec.ConfigValue<Boolean> CONFIG_VALUE_BEHAVIOUR_AUTOSTART;
    public static ForgeConfigSpec.ConfigValue<Boolean> CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY;

    static {
        CONFIG_BUILDER.push("Config for DAE2!");

        CONFIG_VALUE_REDIS_HOST = CONFIG_BUILDER.define("REDIS_HOST", pendingConfig.getHost());
        CONFIG_VALUE_REDIS_PORT = CONFIG_BUILDER.define("REDIS_PORT", pendingConfig.getPort());
        CONFIG_VALUE_REDIS_USERNAME = CONFIG_BUILDER.define("REDIS_USERNAME", pendingConfig.getUsername());
        CONFIG_VALUE_REDIS_PASSWORD = CONFIG_BUILDER.define("REDIS_PASSWORD", pendingConfig.getPassword());

        CONFIG_VALUE_BEHAVIOUR_AUTOSTART = CONFIG_BUILDER.define("BEHAVIOUR_AUTOSTART", pendingConfig.getAutostart());
        CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY = CONFIG_BUILDER.define("BEHAVIOUR_VIRTUAL_INVENTORY", pendingConfig.getVirtualInventory());

        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }

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
                        .then(Commands.literal("get")
                                .then(Commands.literal("current").executes(context -> currentConfig(context.getSource())))
                                .then(Commands.literal("pending").executes(context -> pendingConfig(context.getSource()))))
                        .then(Commands.literal("set").then(Commands.argument("host", StringArgumentType.word())
                                .then(Commands.argument("port", IntegerArgumentType.integer())
                                        .then(Commands.argument("username", StringArgumentType.word())
                                                .then(Commands.argument("password", StringArgumentType.word())
                                                        .executes(context -> config(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "host"),
                                                                IntegerArgumentType.getInteger(context, "port"),
                                                                StringArgumentType.getString(context, "username"),
                                                                StringArgumentType.getString(context, "password")
                                                        )))))))
                        .then(Commands.literal("apply").executes(context -> apply(context.getSource())))
                        .then(Commands.literal("cancel").executes(context -> cancel(context.getSource())))
                        .then(Commands.literal("reset").executes(context -> reset(context.getSource()))))
        );
    }

    private static int config(CommandSourceStack commandSourceStack, String host, int port, String username, String password) {
        pendingConfig = new Config(host, port, username, password);
        IsApplied = false;

        commandSourceStack.sendSystemMessage(Component.literal(currentConfig.toString()));
        commandSourceStack.sendSystemMessage(Component.literal("Use '/dae2 config apply' to save the changes to the current config!"));

        return 1;
    }

    private static int currentConfig(CommandSourceStack commandSourceStack) {
        commandSourceStack.sendSystemMessage(Component.literal(currentConfig.toString()));
        return 1;
    }

    private static int pendingConfig(CommandSourceStack commandSourceStack) {
        commandSourceStack.sendSystemMessage(Component.literal(pendingConfig.toString()));
        return 1;
    }

    private static int status(CommandSourceStack commandSourceStack) {
        commandSourceStack.sendSystemMessage(Component.literal(
                "IsRunning: " + IsRunning + "\n" +
                        "IsApplied: " + IsApplied + "\n" +
                        "Autostart: " + currentConfig.autostart + "\n" +
                        "VirtualInventory: " + currentConfig.getVirtualInventory() + ""
        ));

        return 1;
    }

    private static int apply(CommandSourceStack commandSourceStack) {
        if(IsApplied) {
            commandSourceStack.sendSystemMessage(Component.literal("Config already applied!"));
        } else {
            currentConfig = pendingConfig;
            commandSourceStack.sendSystemMessage(Component.literal("Applied pending config!"));
            IsApplied = true;
        }
        return 1;
    }

    private static int cancel(CommandSourceStack commandSourceStack) {
        IsApplied = true;
        loadConfig();
        commandSourceStack.sendSystemMessage(Component.literal("Canceled pending config!"));
        return 1;
    }

    private static int reset(CommandSourceStack commandSourceStack) {
        currentConfig = Config.getDefault();
        pendingConfig = Config.getDefault();
        IsApplied = true;

        if(IsRunning) {
            stop(commandSourceStack);
        }

        commandSourceStack.sendSystemMessage(Component.literal("Reset config and stopped!"));
        return 1;
    }

    private static int start(CommandSourceStack commandSourceStack) {
        if(IsRunning) {
            commandSourceStack.sendSystemMessage(Component.literal("Already running!"));
            return 1;
        }
        try {
            onStart();
        } catch (Exception e) {
            commandSourceStack.sendSystemMessage(Component.literal("Failed to start! " + e.getMessage()));
            return 1;
        }
        IsRunning = true;
        commandSourceStack.sendSystemMessage(Component.literal("Starting!"));

        if(!IsApplied) {
            commandSourceStack.sendSystemMessage(Component.literal("Pending config is not applied yet!"));
        }
        return 1;
    }

    private static int stop(CommandSourceStack commandSourceStack) {
        if(!IsRunning) {
            commandSourceStack.sendSystemMessage(Component.literal("Already stopped!"));
            return 1;
        }
        try {
            onStop();
        } catch (Exception e) {
            commandSourceStack.sendSystemMessage(Component.literal("Failed to stop! " + e.getMessage()));
            return 1;
        }
        IsRunning = false;
        commandSourceStack.sendSystemMessage(Component.literal("Stopping!"));

        if(!IsApplied) {
            commandSourceStack.sendSystemMessage(Component.literal("Pending config is not applied yet!"));
        }
        return 1;
    }

    private static int restart(CommandSourceStack commandSourceStack) {
        stop(commandSourceStack);
        start(commandSourceStack);
        return 1;
    }

    private static int autostart(CommandSourceStack commandSourceStack, boolean enabled) {
        if(enabled == currentConfig.getAutostart()) {
            commandSourceStack.sendSystemMessage(Component.literal("Autostart already " + enabled + "!"));
        } else {
            currentConfig.setAutostart(enabled);
            saveConfig();
            commandSourceStack.sendSystemMessage(Component.literal("Set autostart to " + enabled + "!"));
        }
        return 1;
    }

    private static int virtualInventory(CommandSourceStack commandSourceStack, boolean enabled) {
        if(enabled == currentConfig.getVirtualInventory()) {
            commandSourceStack.sendSystemMessage(Component.literal("Virtual inventory already " + enabled + "!"));
        } else {
            currentConfig.setVirtualInventory(enabled);
            saveConfig();
            commandSourceStack.sendSystemMessage(Component.literal("Set virtual inventory to " + enabled + "!"));
        }
        return 1;
    }

    private static void onStart() throws Exception {
        InterDimensionalStorageCell.redisInit();
        if(currentConfig.getVirtualInventory()) VirtualInventory.Init();
    }

    private static void onStop() throws Exception {
        InterDimensionalStorageCell.redisReset();
        VirtualInventory.Reset();
    }

    public static void saveConfig() {
        CONFIG_VALUE_REDIS_HOST.set(currentConfig.getHost());
        CONFIG_VALUE_REDIS_PORT.set(currentConfig.getPort());
        CONFIG_VALUE_REDIS_USERNAME.set(currentConfig.getUsername());
        CONFIG_VALUE_REDIS_PASSWORD.set(currentConfig.getPassword());

        CONFIG_VALUE_BEHAVIOUR_AUTOSTART.set(currentConfig.getAutostart());
        CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.set(currentConfig.getVirtualInventory());
    }

    public static void loadConfig() {
        currentConfig = new Config(
                CONFIG_VALUE_REDIS_HOST.get(),
                CONFIG_VALUE_REDIS_PORT.get(),
                CONFIG_VALUE_REDIS_USERNAME.get(),
                CONFIG_VALUE_REDIS_PASSWORD.get()
        );

        currentConfig.setAutostart(CONFIG_VALUE_BEHAVIOUR_AUTOSTART.get());
        currentConfig.setVirtualInventory(CONFIG_VALUE_BEHAVIOUR_VIRTUAL_INVENTORY.get());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) throws Exception {
        onStop();
        saveConfig();
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) throws IOException {
        loadConfig();

        if(currentConfig.getAutostart()) {
            try {
                onStart();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class VirtualInventory {
        public static JedisPool jedisPool;
        public static  Jedis outerJedis;
        public static Jedis innerJedis;
        public static JedisPubSub pubSub;
        public static Thread thread;

        public static void Init() {
            ConfigCommand.Config config = ConfigCommand.currentConfig;

            jedisPool = new JedisPool(
                    config.getHost(),
                    config.getPort(),
                    config.getUsername(),
                    config.getPassword()
            );

            outerJedis = jedisPool.getResource();
            innerJedis = jedisPool.getResource();

            pubSub = new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    try {
                        CompoundTag compoundTag = TagParser.parseTag(message);
                        String index = compoundTag.getString("Index");
                        compoundTag.remove("Index");

                        String UUID = compoundTag.getString("UUID");
                        compoundTag.remove("UUID");

                        Long newAmount = compoundTag.getLong("Amount");
                        if(innerJedis.exists(index)) {
                            CompoundTag currentTag = TagParser.parseTag(innerJedis.get(index));
                            newAmount += currentTag.getLong("Amount");
                        }
                        compoundTag.putLong("Amount", newAmount);

                        if(newAmount > 0) innerJedis.set(index, compoundTag.getAsString());
                        else innerJedis.del(index);
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            thread = new Thread(() ->{
              outerJedis.subscribe(pubSub, "0.inv");
            });

            thread.start();
        }

        public static void Reset() throws InterruptedException {
            if(pubSub != null) {
                pubSub.unsubscribe("0.inv");
                pubSub.unsubscribe();
                pubSub = null;
            }

            if(innerJedis != null) {
                innerJedis.disconnect();
                innerJedis.close();
                innerJedis = null;
            }

            if(thread != null) {
                thread.join();
            }

            if(outerJedis != null) {
                outerJedis.disconnect();
                outerJedis.close();
                outerJedis = null;
            }

            if(jedisPool != null) {
                jedisPool.close();
                jedisPool.destroy();
                jedisPool = null;
            }
        }

    }

    public static class Config {
        private String host;
        private int port;
        private String username;
        private String password;

        private boolean autostart = false;
        private boolean virtualInventory = false;

        public Config(String data) throws CommandSyntaxException {
            CompoundTag compoundTag = TagParser.parseTag(data);
            host = compoundTag.getString("host");
            port = compoundTag.getInt("port");
            username = compoundTag.getString("username");
            password = compoundTag.getString("password");
        }

        public Config(String host, int port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        public static Config getDefault() {
            return new Config("localhost", 6379, "kevin", "klein");
        }

        public boolean getVirtualInventory() {
            return virtualInventory;
        }

        public void setVirtualInventory(boolean virtualInventory) {
            this.virtualInventory = virtualInventory;
        }

        public boolean getAutostart() {
            return autostart;
        }

        public void setAutostart(boolean autostart) {
            this.autostart = autostart;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String toData() {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("host", host);
            compoundTag.putInt("port", port);
            compoundTag.putString("username", username);
            compoundTag.putString("password", password);
            return compoundTag.toString();
        }

        @Override
        public String toString() {
            return "" +
                    "Config:\n" +
                    "  Host: " + host + "\n" +
                    "  Port: " + port + "\n" +
                    "  Username: " + username + "\n" +
                    "  Password: " + password + "";
        }
    }

}
