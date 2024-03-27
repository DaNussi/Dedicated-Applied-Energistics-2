package net.nussi.dedicated_applied_energistics.providers.virtualdisk;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.mojang.logging.LogUtils;
import com.rabbitmq.client.*;
import net.minecraft.network.chat.Component;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.availablestacks.AvailableStacksRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.availablestacks.AvailableStacksResponse;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.description.DescriptionRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.description.DescriptionResponse;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.extract.ExtractRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.extract.ExtractResponse;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert.InsertRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert.InsertResponse;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class VirtualDiskClient implements MEStorage {
    private static final Logger LOGGER = LogUtils.getLogger();

    private InterDimensionalInterfaceBlockEntity instance;
    private String channel;
    private Jedis redis;
    private Channel rabbitmq;

    private VirtualDiskInfo info;

    public VirtualDiskClient(String channel, InterDimensionalInterfaceBlockEntity instance) {
        this.channel = channel;
        this.instance = instance;
    }

    public void onStart() {
        this.redis = instance.getRedis();
        this.rabbitmq = instance.getRabbitmq();

        boolean initRedisSuccess = initRedis();
        if (!initRedisSuccess) {
            onStop();
            return;
        }

        LOGGER.warn("Started virtual drive client " + channel);
    }

    public void onStop() {

        LOGGER.warn("Stopped virtual drive client " + channel);
    }

    private boolean initRedis() {
        try {
            String data = this.redis.hget(VirtualDiskInfo.REDIS_PATH, channel);
            this.info = VirtualDiskInfo.fromString(data);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to fetch VirtualDiskClient info for " + channel);
            return false;
        }
    }


    public String getChannel() {
        return channel;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VirtualDiskClient virtualDiskClient) {
            return virtualDiskClient.channel.equals(this.channel);
        }
        return false;
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return false;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return 0;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {

    }

    @Override
    public Component getDescription() {
        return Component.literal("DAE2 encountered an error!");
    }
}
