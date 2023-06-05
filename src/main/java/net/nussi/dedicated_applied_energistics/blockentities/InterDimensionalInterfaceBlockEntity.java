package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.StorageCell;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.blockentity.inventory.AppEngCellInventory;
import appeng.core.definitions.AEItems;
import appeng.me.storage.DriveWatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import net.nussi.dedicated_applied_energistics.init.ItemInit;
import org.slf4j.Logger;

public class InterDimensionalInterfaceBlockEntity extends AENetworkInvBlockEntity implements IStorageProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    AppEngCellInventory inv = new AppEngCellInventory(this, 1);
    DriveWatcher driveWatcher;

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get(), pos, state);

        this.getMainNode()
                .addService(IStorageProvider.class, this)
                .setFlags(GridFlags.REQUIRE_CHANNEL);

    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return inv;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {

    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        ItemStack is = new ItemStack(ItemInit.INTER_DIMENSIONAL_STORAGE_CELL_ITEM.get());
        inv.setItemDirect(0, is);

        if(is == null) throw new RuntimeException("is is null");

        StorageCell cell = StorageCells.getCellInventory(is, this::onCellContentChanged);

        if(cell == null) throw new RuntimeException("cell is null");

        inv.setHandler(0, cell);
        driveWatcher = new DriveWatcher(cell, this::driveWatcherActivityCallback);

        if(driveWatcher == null) throw new RuntimeException("driveWatcher is null");


        storageMounts.mount(driveWatcher);
    }

    public void driveWatcherActivityCallback() {

    }

    public void onCellContentChanged() {

    }

}
