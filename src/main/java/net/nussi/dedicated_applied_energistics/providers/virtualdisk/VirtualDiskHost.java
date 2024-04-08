package net.nussi.dedicated_applied_energistics.providers.virtualdisk;

import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;
import com.mojang.logging.LogUtils;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import net.nussi.dedicated_applied_energistics.misc.RedisHelper;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.availablestacks.AvailableStacksPair;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.availablestacks.AvailableStacksRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.availablestacks.AvailableStacksResponse;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.description.DescriptionPair;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.description.DescriptionRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.description.DescriptionResponse;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.extract.ExtractPair;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.extract.ExtractRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.extract.ExtractResponse;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert.InsertPair;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert.InsertRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert.InsertResponse;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.preferredstorage.PreferredStoragePair;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.preferredstorage.PreferredStorageRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.preferredstorage.PreferredStorageResponse;
import net.nussi.dedicated_applied_energistics.redsocket.RedsocketFunction;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics.REDSOCKET;

public class VirtualDiskHost {
    private static final Logger LOGGER = LogUtils.getLogger();

    private InterDimensionalInterfaceBlockEntity instance;

    private Jedis redis;
    private String channel;

    public MEStorage storage;
    private VirtualDiskInfo info;


    private final Queue<InsertPair> insertQueue = new LinkedList<>();
    private final Queue<ExtractPair> extractQueue = new LinkedList<>();
    private final Queue<AvailableStacksPair> availableStacksQueue = new LinkedList<>();
    private final Queue<DescriptionPair> descriptionQueue = new LinkedList<>();
    private final Queue<PreferredStoragePair> preferredStorageQueue = new LinkedList<>();

    public VirtualDiskHost(MEStorage storage, InterDimensionalInterfaceBlockEntity instance) {
        this.storage = storage;
        this.info = new VirtualDiskInfo(
                instance.getUuid(),
                instance.getUuid()+"/insert",
                instance.getUuid()+"/extract",
                instance.getUuid()+"/availableStacks",
                instance.getUuid()+"/description",
                instance.getUuid()+"/preferredStorage"
                );
        this.instance = instance;
        this.channel = RedisHelper.getPath(storage, instance);
    }

    public void onStart() {
        this.redis = instance.getRedis();

        this.updateRedis();
        this.initRedsocket();

        LOGGER.warn("Started virtual drive host " + channel);
    }

    public void onStop() {
        this.removeRedis();
        LOGGER.warn("Stopped virtual drive host " + channel);
    }

    private void updateRedis() {
        this.redis.hset(VirtualDiskInfo.REDIS_PATH, channel, this.info.toString());
    }

    private void removeRedis() {
        this.redis.hdel(VirtualDiskInfo.REDIS_PATH, channel);
    }

    private void initRedsocket() {
        try {

            REDSOCKET.registerFunction(this.info.getInsertMethodID(), new RedsocketFunction<String, String>() {
                @Override
                protected String call(String requestText) throws Exception {
                    InsertRequest request = new InsertRequest(requestText.getBytes(StandardCharsets.UTF_8));
                    CompletableFuture<InsertResponse> responseFuture = new CompletableFuture<>();
                    InsertPair pair = new InsertPair(request, responseFuture);
                    insertQueue.add(pair);
                    return new String(responseFuture.get().toBytes(), StandardCharsets.UTF_8);
                }
            });

            REDSOCKET.registerFunction(this.info.getExtractMethodID(), new RedsocketFunction<String, String>() {
                @Override
                protected String call(String requestText) throws Exception {
                    ExtractRequest request = new ExtractRequest(requestText.getBytes(StandardCharsets.UTF_8));
                    CompletableFuture<ExtractResponse> responseFuture = new CompletableFuture<>();
                    ExtractPair pair = new ExtractPair(request, responseFuture);
                    extractQueue.add(pair);
                    return new String(responseFuture.get().toBytes(), StandardCharsets.UTF_8);
                }
            });


            REDSOCKET.registerFunction(this.info.getAvailableStacksMethodID(), new RedsocketFunction<String, String>() {
                @Override
                protected String call(String requestText) throws Exception {
                    AvailableStacksRequest request = new AvailableStacksRequest(requestText.getBytes(StandardCharsets.UTF_8));
                    CompletableFuture<AvailableStacksResponse> responseFuture = new CompletableFuture<>();
                    AvailableStacksPair pair = new AvailableStacksPair(request, responseFuture);
                    availableStacksQueue.add(pair);
                    return new String(responseFuture.get().toBytes(), StandardCharsets.UTF_8);
                }
            });


            REDSOCKET.registerFunction(this.info.getDescriptionMethodID(), new RedsocketFunction<String, String>() {
                @Override
                protected String call(String requestText) throws Exception {
                    DescriptionRequest request = new DescriptionRequest(requestText.getBytes(StandardCharsets.UTF_8));
                    CompletableFuture<DescriptionResponse> responseFuture = new CompletableFuture<>();
                    DescriptionPair pair = new DescriptionPair(request, responseFuture);
                    descriptionQueue.add(pair);
                    return new String(responseFuture.get().toBytes(), StandardCharsets.UTF_8);
                }
            });


            REDSOCKET.registerFunction(this.info.getPreferredStorageMethodID(), new RedsocketFunction<String, String>() {
                @Override
                protected String call(String requestText) throws Exception {
                    PreferredStorageRequest request = new PreferredStorageRequest(requestText.getBytes(StandardCharsets.UTF_8));
                    CompletableFuture<PreferredStorageResponse> responseFuture = new CompletableFuture<>();
                    PreferredStoragePair pair = new PreferredStoragePair(request, responseFuture);
                    preferredStorageQueue.add(pair);
                    return new String(responseFuture.get().toBytes(), StandardCharsets.UTF_8);
                }
            });


        } catch (Exception e) {
            LOGGER.error("Failed to init websocket");
            e.printStackTrace();
        }

    }

    public void onTick() {
//        LOGGER.info("Ticking");

        while (insertQueue.size() > 0) {
            InsertPair pair = insertQueue.poll();
            InsertRequest request = pair.getRequest();
            long data = this.storage.insert(request.what, request.amount, request.mode, IActionSource.ofMachine(instance));
            InsertResponse response = new InsertResponse(request.getId(), true, data);
            pair.getResponseFuture().complete(response);
        }

        while (extractQueue.size() > 0) {
            ExtractPair pair = extractQueue.poll();
            ExtractRequest request = pair.getRequest();
            long data = this.storage.insert(request.what, request.amount, request.mode, IActionSource.ofMachine(instance));
            ExtractResponse response = new ExtractResponse(request.getId(), true, data);
            pair.getResponseFuture().complete(response);
        }

        while (availableStacksQueue.size() > 0) {
            AvailableStacksPair pair = availableStacksQueue.poll();
            AvailableStacksRequest request = pair.getRequest();
            var data = this.storage.getAvailableStacks();
            AvailableStacksResponse response = new AvailableStacksResponse(request.getId(), true, data);
            pair.getResponseFuture().complete(response);
        }

        while (descriptionQueue.size() > 0) {
            DescriptionPair pair = descriptionQueue.poll();
            DescriptionRequest request = pair.getRequest();
            var data = this.storage.getDescription().getString();
            DescriptionResponse response = new DescriptionResponse(request.getId(), true, data);
            pair.getResponseFuture().complete(response);
        }

        while (preferredStorageQueue.size() > 0) {
            PreferredStoragePair pair = preferredStorageQueue.poll();
            PreferredStorageRequest request = pair.getRequest();
            var data = this.storage.isPreferredStorageFor(request.getWhat(), IActionSource.ofMachine(instance));
            PreferredStorageResponse response = new PreferredStorageResponse(request.getId(), true, data);
            pair.getResponseFuture().complete(response);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VirtualDiskHost virtualDiskHost) {
            return virtualDiskHost.channel.equals(this.channel);
        }
        return false;
    }

    public String getChannel() {
        return channel;
    }


}
