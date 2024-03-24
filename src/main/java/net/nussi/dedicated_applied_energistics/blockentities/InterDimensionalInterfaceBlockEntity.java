package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.orientation.BlockOrientation;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import net.nussi.dedicated_applied_energistics.init.ItemInit;
import net.nussi.dedicated_applied_energistics.providers.InterDimensionalInterfaceCrafting;
import net.nussi.dedicated_applied_energistics.providers.InterDimensionalInterfaceStorage;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InterDimensionalInterfaceBlockEntity extends AENetworkBlockEntity implements IStorageProvider, ICraftingProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final HashMap<BlockPos ,InterDimensionalInterfaceBlockEntity> blockEntities = new HashMap<>();

    private InterDimensionalInterfaceStorage storage;
    private InterDimensionalInterfaceCrafting crafting;

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get(), pos, state);

        getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setFlags(GridFlags.DENSE_CAPACITY)
                .setVisualRepresentation(AEItemKey.of(new ItemStack(ItemInit.INTER_DIMENSIONAL_INTERFACE_BLOCK_ITEM.get())))
                .setIdlePowerUsage(1000);


        this.storage = new InterDimensionalInterfaceStorage(this);
        this.getMainNode().addService(IStorageProvider.class, this);

        this.crafting = new InterDimensionalInterfaceCrafting(this);
        this.getMainNode().addService(ICraftingProvider.class, this);


        // Index Block Entity
        var oldBlockEntity = blockEntities.get(pos);
        if(oldBlockEntity != null) {
            oldBlockEntity.onStop();
        }
        blockEntities.put(pos, this);
    }

    public boolean running = false;

    public void onStart() {
        if(this.running) return;
        this.running = true;

        this.storage.onStart();
        this.crafting.onStart();
    }

    public void onStop() {
        if(!this.running) return;
        this.running = false;

        this.storage.onStop();
        this.crafting.onStop();
    }



    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        storage.mountInventories(storageMounts);
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return crafting.getAvailablePatterns();
    }

    @Override
    public int getPatternPriority() {
        return crafting.getPatternPriority();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        return crafting.pushPattern(patternDetails, inputHolder);
    }

    @Override
    public boolean isBusy() {
        return crafting.isBusy();
    }

    @Override
    public Set<AEKey> getEmitableItems() {
        return crafting.getEmitableItems();
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


        // TEST

        try {
            var nodes = this.getMainNode().getGrid().getNodes();

            int i = 0;
            for(var node : nodes) {
                LOGGER.info(" | Found " + i++ + " " + node.getVisualRepresentation().toString());
                try {
                    KeyCounter itemKeys = new KeyCounter();
                    IStorageProvider provider = node.getService(IStorageProvider.class);
                    IStorageMounts mounts = (inventory, priority) -> {
                        inventory.getAvailableStacks(itemKeys);
                    };
                    provider.mountInventories(mounts);

                    LOGGER.info(" | Found items " + itemKeys);

                } catch (Exception e) {
                    LOGGER.info(" | Failed to index node");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            LOGGER.info(" | Failed to get nodes");
            e.printStackTrace();
        }
    }


    // Loading / Unloading Hanndling

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
