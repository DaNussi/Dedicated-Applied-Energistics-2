package net.nussi.dedicated_applied_energistics.blockentities;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import org.slf4j.Logger;

import java.util.UUID;

public class TestBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    public String ID = UUID.randomUUID().toString();

    public TestBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.TEST_BLOCK_ENTITY_TYPE.get(), pos, state);
    }

    public void onCreate() {
        LOGGER.info("onCreate " + this);
    }

    public void onDestroy() {
        LOGGER.info("onDestroy" + this);
    }

    @Override
    public String toString() {
        return "TestBlockEntity {ID: " + ID + "}";
    }
}
