package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.config.AccessRestriction;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.StorageCell;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.blockentity.inventory.AppEngCellInventory;
import appeng.core.definitions.AEItems;
import appeng.me.cells.BasicCellHandler;
import appeng.me.cells.CreativeCellHandler;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.NullInventory;
import appeng.parts.storagebus.StorageBusPart;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import org.slf4j.Logger;

public class TestBlockEntity extends AENetworkInvBlockEntity implements IStorageProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    private AppEngCellInventory inv = new AppEngCellInventory(this, 1);

    public TestBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityTypeInit.TEST_ENTITY_TYPE.get(), pos, blockState);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return inv;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        LOGGER.info("onChangeInventory " + slot + " " + inv.toString());
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {

        StorageCell storageCell = BasicCellHandler.INSTANCE.getCellInventory(AEItems.ITEM_CELL_256K.stack(), null);
        inv.setHandler(0, storageCell);
        StorageBusInventory handler = new StorageBusInventory(storageCell);
        storageMounts.mount(handler);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    static class StorageBusInventory extends MEInventoryHandler {
        public StorageBusInventory(MEStorage inventory) {
            super(inventory);
        }

        @Override
        protected MEStorage getDelegate() {
            return super.getDelegate();
        }

        @Override
        protected void setDelegate(MEStorage delegate) {
            super.setDelegate(delegate);
        }

        public void setAccessRestriction(AccessRestriction setting) {
            setAllowExtraction(setting.isAllowExtraction());
            setAllowInsertion(setting.isAllowInsertion());
        }
    }
}
