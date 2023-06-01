package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.config.AccessRestriction;
import appeng.api.inventories.BaseInternalInventory;
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
import appeng.util.inv.AppEngInternalInventory;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import org.slf4j.Logger;

import java.util.HashMap;

public class TestBlockEntity extends AENetworkInvBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    private DedicatedInternalInventory inv = new DedicatedInternalInventory();

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


    public static class DedicatedInternalInventory extends BaseInternalInventory {

        HashMap<Integer, ItemStack> inventory = new HashMap<>();

        public DedicatedInternalInventory() {

        }

        @Override
        public int size() {
            return inventory.size() + 1;
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            ItemStack itemStack = inventory.get(slotIndex);
            if(itemStack == null) itemStack = ItemStack.EMPTY;
            return itemStack;
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            inventory.put(slotIndex, stack);
        }
    }

}
