package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.blockentity.inventory.AppEngCellInventory;
import appeng.core.definitions.AEItems;
import appeng.me.storage.DriveWatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import net.nussi.dedicated_applied_energistics.init.ItemInit;
import net.nussi.dedicated_applied_energistics.items.InterDimensionalStorageCell;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class InterDimensionalInterfaceBlockEntity extends AENetworkInvBlockEntity implements IStorageProvider, StorageCell {
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
        return new AppEngCellInventory(this, 0);
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {

    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        InterDimensionalStorageCell item = ItemInit.INTER_DIMENSIONAL_STORAGE_CELL_ITEM.get();
        item.blockEntity = this;
        ItemStack is = new ItemStack(item);

        inv.setItemDirect(0, is);

        if(is == null) throw new RuntimeException("is is null");

        StorageCell cell = StorageCells.getCellInventory(is, () -> {});

        if(cell == null) throw new RuntimeException("cell is null");

        inv.setHandler(0, cell);
        driveWatcher = new DriveWatcher(cell, () -> {});

        if(driveWatcher == null) throw new RuntimeException("driveWatcher is null");


        storageMounts.mount(driveWatcher);

        inv.setItemDirect(0, ItemStack.EMPTY);
    }


    @Override
    public CellState getStatus() {
        return CellState.EMPTY;
    }

    @Override
    public double getIdleDrain() {
            return 0;
    }

    @Override
    public void persist() {

    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return true;
    }


    HashMap<AEKey, Long> items = new HashMap<>();

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {

        if(items.containsKey(what)) {
            long a = items.get(what);
            a += amount;
            if(!mode.isSimulate()) items.put(what, a);
        } else {
            if(!mode.isSimulate()) items.put(what, amount);
        }

        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if(items.containsKey(what)) {
            long a = items.get(what);

            if(a > amount) {
                a -= amount;
                if(!mode.isSimulate()) items.put(what, a);
                return amount;
            } else if ( a == amount) {
                if(!mode.isSimulate()) items.remove(what);
                return amount;
            } else {
                if(!mode.isSimulate()) items.remove(what);
                return a;
            }
        } else {
            return 0;
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for(Map.Entry<AEKey, Long> pair : items.entrySet()) {
            out.add(pair.getKey(), pair.getValue());
        }
    }

    @Override
    public Component getDescription() {
        return Component.literal("Inter Dimensional Interface");
    }

}
