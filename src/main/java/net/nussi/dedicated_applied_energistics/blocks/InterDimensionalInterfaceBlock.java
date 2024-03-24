package net.nussi.dedicated_applied_energistics.blocks;

import appeng.init.InitBlockEntities;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;

import static net.minecraft.world.level.levelgen.structure.Structure.simpleCodec;
import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InterDimensionalInterfaceBlock extends Block implements EntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final HashMap<BlockPos, InterDimensionalInterfaceBlockEntity> blockEntities = new HashMap<>();

    public InterDimensionalInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        InterDimensionalInterfaceBlockEntity blockEntity = blockEntities.get(pos);
        if(blockEntity != null) {
            blockEntity.onStop();
        }
        blockEntity = BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get().create(pos, state);
        blockEntities.put(pos, blockEntity);

        return blockEntity;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get() ? InterDimensionalInterfaceBlockEntity::tick : null;
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
