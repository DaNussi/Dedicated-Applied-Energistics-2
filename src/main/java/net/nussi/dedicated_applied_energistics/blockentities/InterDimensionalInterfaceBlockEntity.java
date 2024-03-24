package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.blockentity.grid.AENetworkBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import net.nussi.dedicated_applied_energistics.providers.InterDimensionalInterfaceCrafting;
import net.nussi.dedicated_applied_energistics.providers.InterDimensionalInterfaceStorage;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics.MODID;

public class InterDimensionalInterfaceBlockEntity extends AENetworkBlockEntity implements IStorageProvider, ICraftingProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    private InterDimensionalInterfaceStorage storage;
    private InterDimensionalInterfaceCrafting crafting;

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get(), pos, state);

        this.storage = new InterDimensionalInterfaceStorage(this);
        this.getMainNode().addService(IStorageProvider.class, this);

        this.crafting = new InterDimensionalInterfaceCrafting(this);
        this.getMainNode().addService(ICraftingProvider.class, this);

    }

    // Inside MyBlockEntity
    public static void tick(Level level, BlockPos pos, BlockState state, InterDimensionalInterfaceBlockEntity blockEntity) {
        // Do stuff
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

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {

    }
}
