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
        try {
            InsertRequest action = new InsertRequest(what, amount, mode);

            String replyQueueName = rabbitmq.queueDeclare().getQueue();
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(action.getId()).replyTo(replyQueueName).build();


            CompletableFuture<InsertResponse> responseFuture = new CompletableFuture<>();
            responseFuture.completeOnTimeout(new InsertResponse("", false, 0), 5, TimeUnit.SECONDS);
            DeliverCallback insertCallback = ((consumerTag, delivery) -> {
                if (!delivery.getProperties().getCorrelationId().equals(action.getId())) return;
                InsertResponse response = new InsertResponse(action.getId(), false, 0);
                try {
                    response = new InsertResponse(delivery.getBody());
                } catch (Exception e) {
                    LOGGER.error("Failed to pares response");
                    e.printStackTrace();
                }
                responseFuture.complete(response);
            });

            String ctag = rabbitmq.basicConsume(replyQueueName, true, insertCallback, consumerTag -> {});

//            LOGGER.info("Calling action " + action);
            rabbitmq.basicPublish("", channel + "/insert", props, action.toBytes());

            InsertResponse response = responseFuture.get();
            rabbitmq.basicCancel(ctag);
//            LOGGER.info("Callback for action " + response);
            if (response.isSuccess()) {
                return response.data;
            } else {
                LOGGER.error("Request was unsuccessful " + response);
                return 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        try {
            ExtractRequest action = new ExtractRequest(what, amount, mode);

            String replyQueueName = rabbitmq.queueDeclare().getQueue();
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(action.getId()).replyTo(replyQueueName).build();

            CompletableFuture<ExtractResponse> responseFuture = new CompletableFuture<>();
            responseFuture.completeOnTimeout(new ExtractResponse("", false, 0), 5, TimeUnit.SECONDS);
            DeliverCallback insertCallback = ((consumerTag, delivery) -> {
                if (!delivery.getProperties().getCorrelationId().equals(action.getId())) return;
                ExtractResponse response = new ExtractResponse(action.getId(), false, 0);
                try {
                    response = new ExtractResponse(delivery.getBody());
                } catch (Exception e) {
                    LOGGER.error("Failed to pares response");
                    e.printStackTrace();
                }
                responseFuture.complete(response);
            });

            String ctag = rabbitmq.basicConsume(replyQueueName, true, insertCallback, consumerTag -> {
            });

//            LOGGER.info("Calling action " + action);
            rabbitmq.basicPublish("", channel + "/extract", props, action.toBytes());

            ExtractResponse response = responseFuture.get();
            rabbitmq.basicCancel(ctag);
//            LOGGER.info("Callback for action " + response);
            if (response.isSuccess()) {
                return response.data;
            } else {
                LOGGER.error("Request was unsuccessful " + response);
                return 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        try {
            AvailableStacksRequest action = new AvailableStacksRequest();

            String replyQueueName = rabbitmq.queueDeclare().getQueue();
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(action.getId()).replyTo(replyQueueName).build();

            CompletableFuture<AvailableStacksResponse> responseFuture = new CompletableFuture<>();
            responseFuture.completeOnTimeout(new AvailableStacksResponse("", false), 5, TimeUnit.SECONDS);
            DeliverCallback insertCallback = ((consumerTag, delivery) -> {
                if (!delivery.getProperties().getCorrelationId().equals(action.getId())) return;
                try {
                    AvailableStacksResponse response = new AvailableStacksResponse(delivery.getBody());
                    responseFuture.complete(response);
                } catch (Exception e) {
                    LOGGER.error("Failed to pares response");
                    e.printStackTrace();
                    AvailableStacksResponse response = new AvailableStacksResponse(action.getId(), false);
                    responseFuture.complete(response);
                }
            });

            String ctag = rabbitmq.basicConsume(replyQueueName, true, insertCallback, consumerTag -> {
            });

//            LOGGER.info("Calling action " + action);
            rabbitmq.basicPublish("", channel + "/getAvailableStacks", props, action.toBytes());

            AvailableStacksResponse response = responseFuture.get();
            rabbitmq.basicCancel(ctag);
//            LOGGER.info("Callback for action " + response);
            if (response.isSuccess()) {
                response.getAvailableStacks(out);
            } else {
                LOGGER.error("Request was unsuccessful " + response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Component getDescription() {
        try {
            DescriptionRequest action = new DescriptionRequest();

            String replyQueueName = rabbitmq.queueDeclare().getQueue();
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(action.getId()).replyTo(replyQueueName).build();

            CompletableFuture<DescriptionResponse> responseFuture = new CompletableFuture<>();
            responseFuture.completeOnTimeout(new DescriptionResponse("", false, ""), 5, TimeUnit.SECONDS);
            DeliverCallback insertCallback = ((consumerTag, delivery) -> {
                if (!delivery.getProperties().getCorrelationId().equals(action.getId())) return;
                DescriptionResponse response = new DescriptionResponse(action.getId(), false, "");
                try {
                    response = new DescriptionResponse(delivery.getBody());
                } catch (Exception e) {
                    LOGGER.error("Failed to pares response");
                    e.printStackTrace();
                }
                responseFuture.complete(response);
            });

            String ctag = rabbitmq.basicConsume(replyQueueName, true, insertCallback, consumerTag -> {
            });

//            LOGGER.info("Calling action " + action);
            rabbitmq.basicPublish("", channel + "/extract", props, action.toBytes());

            DescriptionResponse response = responseFuture.get();
            rabbitmq.basicCancel(ctag);
//            LOGGER.info("Callback for action " + response);
            if (response.isSuccess()) {
                return Component.literal(response.getDescription());
            } else {
                LOGGER.error("Request was unsuccessful " + response);
                return Component.literal("DAE2 encountered an error!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Component.literal("DAE2 encountered an error!");
        }
    }
}
