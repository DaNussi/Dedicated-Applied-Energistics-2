package net.nussi.dedicated_applied_energistics.blocks;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import net.nussi.dedicated_applied_energistics.blockentities.TestBlockEntity;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import net.nussi.dedicated_applied_energistics.init.BlockInit;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mod.EventBusSubscriber(modid = DedicatedAppliedEnegistics.MODID)
public class InterDimensionalInterfaceBlock extends Block implements EntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static List<InterDimensionalInterfaceBlockEntity> blockEntities = new ArrayList<>();
    private InterDimensionalInterfaceBlockEntity blockEntity;

    public InterDimensionalInterfaceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if(blockEntity != null) {
            blockEntity.onDestroy();
            blockEntity = null;
        }

        blockEntity = BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get().create(pos, state);
        blockEntity.onCreate();

        return blockEntity;
    }

    @Override
    public void onRemove(BlockState p_60515_, Level p_60516_, BlockPos pos, BlockState p_60518_, boolean p_60519_) {
        if(blockEntity != null) blockEntity.onDestroy();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        for(InterDimensionalInterfaceBlockEntity b : blockEntities) {
            b.onDestroy();
        }

        blockEntities.clear();
    }

}
