package net.nussi.dedicated_applied_energistics.blocks;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import org.slf4j.Logger;

public class InterDimensionalInterfaceBlock extends AEBaseEntityBlock<InterDimensionalInterfaceBlockEntity> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public InterDimensionalInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.none();
    }

}
