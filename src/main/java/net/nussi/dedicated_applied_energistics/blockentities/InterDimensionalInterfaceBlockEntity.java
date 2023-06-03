package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.AEBaseInvBlockEntity;
import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import org.slf4j.Logger;

import java.util.HashMap;

public class InterDimensionalInterfaceBlockEntity extends AEBaseInvBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final InternalInventory internalInventory = new DedicatedInternalInventory();

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get(), pos, state);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return internalInventory;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        LOGGER.info("onChangeInventory " + slot + " " + inv.getSlotInv(slot));
    }


    public static class DedicatedInternalInventory extends BaseInternalInventory {
        HashMap<Integer, ItemStack> internalInventory = new HashMap<>();

        @Override
        public int size() {
            return internalInventory.size() + 1;
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            ItemStack out = internalInventory.get(slotIndex);
            if(out == null) return ItemStack.EMPTY;
            return out;
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            if(stack.getCount() == 0) {
                internalInventory.remove(slotIndex);
                return;
            }
            internalInventory.put(slotIndex, stack);
            LOGGER.info("Stack: " + slotIndex + " " + stack.getItem() + " x " + stack.getCount());
        }


        /**
         * Simular to the Original Method but without the maximum slot size check
         * @param slot
         * @param stack
         * @param simulate
         * @return
         */
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            Preconditions.checkArgument(slot >= 0 && slot < size(), "slot out of range");

            if (stack.isEmpty() || !isItemValid(slot, stack)) {
                return stack;
            }

            var inSlot = getStackInSlot(slot);
            if (!inSlot.isEmpty() && !ItemStack.isSameItemSameTags(inSlot, stack)) {
                return stack;
            }

            // Calculate how much free space there is in the targeted slot, considering
            // an item-dependent maximum stack size, as well as a potential slot-based limit
            int maxSpace = Integer.MAX_VALUE;                                               // Nussi Edit: Modified to not Check for maxSpace!
            int freeSpace = maxSpace - inSlot.getCount();
            if (freeSpace <= 0) {
                return stack;
            }

            var insertAmount = Math.min(stack.getCount(), freeSpace);
            if (!simulate) {
                var newItem = inSlot.isEmpty() ? stack.copy() : inSlot.copy();
                newItem.setCount(inSlot.getCount() + insertAmount);
                setItemDirect(slot, newItem);
            }

            if (freeSpace >= stack.getCount()) {
                return ItemStack.EMPTY;
            } else {
                var r = stack.copy();
                r.shrink(insertAmount);
                return r;
            }
        }
    }

}
