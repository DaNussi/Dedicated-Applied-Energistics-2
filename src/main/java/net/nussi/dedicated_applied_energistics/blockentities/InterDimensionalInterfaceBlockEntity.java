package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.IStorageProvider;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkBlockEntity;
import com.mojang.logging.LogUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import net.nussi.dedicated_applied_energistics.init.ItemInit;
import net.nussi.dedicated_applied_energistics.providers.InterDimensionalInterfaceCrafting;
import net.nussi.dedicated_applied_energistics.providers.InterDimensionalInterfaceStorage;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.util.HashMap;
import java.util.Set;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics.MODID;
import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController.*;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InterDimensionalInterfaceBlockEntity extends AENetworkBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    private InterDimensionalInterfaceStorage storage;
    private InterDimensionalInterfaceCrafting crafting;

    private String uuid;

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get(), pos, state);


        this.uuid = CONFIG_VALUE_HOST_ID.get() + "_" + this.getBlockPos().getX()  + "_" + this.getBlockPos().getY()  + "_" + this.getBlockPos().getZ();

        getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setVisualRepresentation(AEItemKey.of(new ItemStack(ItemInit.INTER_DIMENSIONAL_INTERFACE_BLOCK_ITEM.get())))
                .setIdlePowerUsage(1000);


        this.storage = new InterDimensionalInterfaceStorage(this);
        this.getMainNode().addService(IStorageProvider.class, this.storage);
        this.getMainNode().addService(IGridTickable.class, this.storage);

        this.crafting = new InterDimensionalInterfaceCrafting(this);
        this.getMainNode().addService(ICraftingProvider.class, this.crafting);

        registerBlockEntity(this, pos);
    }

    public String getUuid() {
        return uuid;
    }

    public Channel getRabbitmq() {
        return rabbitmq_channel;
    }

    public Jedis getRedis() {
        return redis_connection;
    }

    public boolean running = false;

    public void onStart() {
        if(this.running) return;
        LOGGER.info(uuid + " | Starting storage provider");

        boolean success = connectDatabase();
        if(!success) return;

        this.storage.onStart();
        this.crafting.onStart();

        this.running = true;
        LOGGER.info(uuid + " | Started storage provider");
    }

    public void onStop() {
        if(!this.running) return;
        LOGGER.info(uuid + " | Stopping storage provider");
        this.running = false;

        this.storage.onStop();
        this.crafting.onStop();

        this.disconnectDatabase();

        LOGGER.info(uuid + " | Stopped storage provider");
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return Set.of(Direction.values());
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        this.storage.onMainNodeStateChanged(reason);
    }

    // Connect Database
    private Connection rabbit_connection;
    private Channel rabbitmq_channel;
    private JedisPool redis_pool;
    private Jedis redis_connection;

    private boolean connectDatabase() {

        for (int i = 0; i <= 5; i++) {
            try {
                LOGGER.debug(uuid + " | Connecting to " + CONFIG_VALUE_RABBITMQ_URI.get());
                ConnectionFactory factory = new ConnectionFactory();
                factory.setUri(CONFIG_VALUE_RABBITMQ_URI.get());


                this.rabbit_connection = factory.newConnection();
                this.rabbitmq_channel = rabbit_connection.createChannel();
                break;
            } catch (Exception e) {
                LOGGER.error(uuid + " | Failed to start rabbit mq connection! Retry " + i + "/5... ");
//                e.printStackTrace();
                if(i == 5) return false;
            }
        }

        for (int i = 0; i <= 5; i++) {
            try {
                LOGGER.debug(uuid + " | Connecting to " + CONFIG_VALUE_REDIS_URI.get());
                redis_pool = new JedisPool(new URI(CONFIG_VALUE_REDIS_URI.get()));
                redis_connection = redis_pool.getResource();
            } catch (Exception e) {
                LOGGER.error(uuid + " | Failed to start redis connection! Retry " + i + "/5... ");
//                e.printStackTrace();
                if(i == 5) return false;
            }
        }
        return true;
    }

    private void disconnectDatabase() {

        if(this.rabbitmq_channel != null && this.rabbitmq_channel.isOpen()) {
            try {
                this.rabbitmq_channel.close();
            } catch (Exception e) {
                LOGGER.error(uuid + " | Failed to close rabbit mq channel");
                e.printStackTrace();
            }
        }

        if(this.rabbit_connection != null && this.rabbit_connection.isOpen()) {
            try {
                this.rabbit_connection.close();
            } catch (Exception e) {
                LOGGER.error(uuid + " | Failed to close rabbit mq connection");
                e.printStackTrace();
            }
        }

        if(this.redis_connection != null && this.redis_connection.isConnected()) {
            try {
                this.redis_connection.close();
            } catch (Exception e) {
                LOGGER.error(uuid + " | Failed to close redis connection");
                e.printStackTrace();
            }
        }


        if(this.redis_pool != null && !this.redis_pool.isClosed()) {
            try {
                this.redis_pool.close();
            } catch (Exception e) {
                LOGGER.error(uuid + " | Failed to close redis pool");
                e.printStackTrace();
            }
        }
    }

    // Loading / Unloading Handling
    public static final HashMap<BlockPos ,InterDimensionalInterfaceBlockEntity> blockEntities = new HashMap<>();

    public static void registerBlockEntity(InterDimensionalInterfaceBlockEntity blockEntity, BlockPos pos) {
        var oldBlockEntity = blockEntities.get(pos);
        if(oldBlockEntity != null) {
            oldBlockEntity.onStop();
        }
        blockEntities.put(pos, blockEntity);
    }

    public static void onStartBlockPos(BlockPos pos) {
        InterDimensionalInterfaceBlockEntity blockEntity = blockEntities.get(pos);
        if(blockEntity == null) {
            LOGGER.debug("Tried to start block entity that dosen't exist.");
            return;
        }
        blockEntity.onStart();
    }

    public static void onStopBlockPos(BlockPos pos) {
        InterDimensionalInterfaceBlockEntity blockEntity = blockEntities.get(pos);
        if(blockEntity == null) {
            LOGGER.debug("Tried to stop block entity that dosen't exist.");
            return;
        }
        blockEntity.onStop();
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        onStartBlockPos(event.getPos());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        onStopBlockPos(event.getPos());
    }

    @SubscribeEvent
    public static void onBlockLoad(ChunkEvent.Load event) {
        for (BlockPos blockEntityPositions : event.getChunk().getBlockEntitiesPos()) {
            onStartBlockPos(blockEntityPositions);
        }
    }

    @SubscribeEvent
    public static void onBlockUnload(ChunkEvent.Unload event) {
        for (BlockPos blockEntityPositions : event.getChunk().getBlockEntitiesPos()) {
            onStopBlockPos(blockEntityPositions);
        }
    }

}
