package net.nussi.dedicated_applied_energistics.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import org.jetbrains.annotations.Nullable;

public class InterDimensionalInterfaceBlock extends Block implements EntityBlock {

    InterDimensionalInterfaceBlockEntity blockEntity;

    public InterDimensionalInterfaceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        this.blockEntity =  BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get().create(pos, state);
        return blockEntity;
    }

}
