package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.nussi.dedicated_applied_energistics.capabilities.TestBlockCapability;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import org.slf4j.Logger;
import appeng.blockentity.grid.AENetworkBlockEntity;

public class TestBlockEntity extends BlockEntity {
    private final TestBlockCapability itemHandler = new TestBlockCapability();
    private final LazyOptional<TestBlockCapability> lazyItemHandler = LazyOptional.of(() -> this.itemHandler);

    private static final Logger LOGGER = LogUtils.getLogger();

    public TestBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.TEST_BLOCK_ENTITY_TYPE.get(), pos, state);
    }

    @Override
    public void load(CompoundTag nbt) {
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        super.load(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(nbt);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction direction) {
        if (capability == ForgeCapabilities.ITEM_HANDLER
        ) {
            return this.lazyItemHandler.cast();
        }
        return super.getCapability(capability, direction); // See note after snippet
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.lazyItemHandler.invalidate();
    }




}
