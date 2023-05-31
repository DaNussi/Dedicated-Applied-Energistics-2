package net.nussi.dedicated_applied_energistics.capabilities;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Vector;

public class InterDimensionalInterfaceCapability implements IItemHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    Vector<ItemStack> items = new Vector<>();

    public InterDimensionalInterfaceCapability() {

    }

    @Override
    public int getSlots() {
        return Integer.MAX_VALUE;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        ItemStack itemStack = null;
        try {
            itemStack = items.get(slot);
        } catch (Exception e) {
            items.add(slot, null);
        }
        if(itemStack == null) return ItemStack.EMPTY;
        return itemStack;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack itemStack, boolean simulate) {

        ItemStack currentItemStack = null;
        try {
            currentItemStack = items.get(slot);
        } catch (Exception e) {
            items.add(slot, null);
        }

        if(currentItemStack == null) {
            currentItemStack = itemStack.copy();
            if(!simulate) items.set(slot, currentItemStack);
            ItemStack out = itemStack.copy();
            out.setCount(0);
            if(!simulate) LOGGER.info("Inserted item " + itemStack.getItem() + " x " + itemStack.getCount());
            return out;
        } else if (currentItemStack.getItem() != itemStack.getItem()) {
            return itemStack;
        } else {
            currentItemStack = currentItemStack.copy();
            currentItemStack.setCount(currentItemStack.getCount() + itemStack.getCount());
            if(!simulate) items.set(slot, currentItemStack);

            ItemStack out = itemStack.copy();
            out.setCount(0);
            if(!simulate) LOGGER.info("Inserted item " + itemStack.getItem() + " x " + itemStack.getCount());
            return out;
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {

        try {
            items.get(slot);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }

        if(items.get(slot) == null) return ItemStack.EMPTY;

        ItemStack currentItemStack = items.get(slot).copy();
        if(currentItemStack.getCount() <= amount) {
            ItemStack out = currentItemStack.copy();
            out.setCount(currentItemStack.getCount());

            if(!simulate) items.set(slot, null);
            if(!simulate) LOGGER.info("Extracted item " + out.getItem() + " x " + out.getCount());
            return out;
        } else {
            currentItemStack.setCount(currentItemStack.getCount() - amount);
            if(!simulate) items.set(slot, currentItemStack);

            ItemStack out = currentItemStack.copy();
            out.setCount(amount);
            if(!simulate) LOGGER.info("Extracted item " + out.getItem() + " x " + out.getCount());
            return out;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        try {
            ItemStack itemStack = items.get(slot);
            if(itemStack == null) return true;
            if(itemStack.getItem() == stack.getItem()) return true;
        } catch (Exception e) {
            return true;
        }

        return false;
    }

    public Tag serializeNBT() {
        CompoundTag compound = new CompoundTag();

        int index = 0;
        for (ItemStack itemStack : items) {
            if(itemStack == null) continue;

            CompoundTag itemTag = itemStack.serializeNBT();
            itemTag.putInt("Count", itemStack.getCount());

            compound.put(String.valueOf(index++), itemTag);
        }
        compound.putInt("InventorySize", index);

        return compound;
    }

    public void deserializeNBT(CompoundTag compound) {
        items = new Vector<>();
        for (int i = 0; i < compound.getInt("InventorySize"); i++) {
            items.add(ItemStack.of(compound.getCompound(String.valueOf(i))));
        }

    }
}
