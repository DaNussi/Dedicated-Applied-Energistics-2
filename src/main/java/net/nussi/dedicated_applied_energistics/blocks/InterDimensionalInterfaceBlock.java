package net.nussi.dedicated_applied_energistics.blocks;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import static net.minecraft.world.level.levelgen.structure.Structure.simpleCodec;

public class InterDimensionalInterfaceBlock extends Block implements EntityBlock {
    private static final Logger LOGGER = LogUtils.getLogger();

    public InterDimensionalInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get().create(pos, state);
    }

}
