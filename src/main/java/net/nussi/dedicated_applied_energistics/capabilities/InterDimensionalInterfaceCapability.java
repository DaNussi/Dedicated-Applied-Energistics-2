package net.nussi.dedicated_applied_energistics.capabilities;

import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.blockentity.AEBaseInvBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Vector;

public class InterDimensionalInterfaceCapability implements IItemHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    Vector<ItemStack> items = new Vector<>();

    public InterDimensionalInterfaceCapability() {

    }

    @Override
    public int getSlots() {
        int amount = items.size()+1;
//        LOGGER.info("GetSlots " + amount);
        return amount;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        ItemStack itemStack = null;
        try {
            itemStack = items.get(slot);
        } catch (Exception e) {
            return ItemStack.EMPTY;
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
//            if(!simulate) UpdatedRedis();
            if(!simulate) SaveItem(currentItemStack);
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
//            if(!simulate) UpdatedRedis();
            if(!simulate) SaveItem(currentItemStack);
            return out;
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {

        try {
            if(items.get(slot) == null) return ItemStack.EMPTY;
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }


        ItemStack currentItemStack = items.get(slot).copy();
        if(currentItemStack.getCount() <= amount) {
            ItemStack out = currentItemStack.copy();
            out.setCount(currentItemStack.getCount());

            if(!simulate) items.remove(slot);
            if(!simulate) LOGGER.info("Extracted item " + out.getItem() + " x " + out.getCount());
//            if(!simulate) UpdatedRedis();
            return out;
        } else {
            currentItemStack.setCount(currentItemStack.getCount() - amount);
            if(!simulate) items.set(slot, currentItemStack);

            ItemStack out = currentItemStack.copy();
            out.setCount(amount);
            if(!simulate) LOGGER.info("Extracted item " + out.getItem() + " x " + out.getCount());
//            if(!simulate) UpdatedRedis();
            return out;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if(stack == null) return false;

        try {
            ItemStack itemStack = items.get(slot);
            if(itemStack == null) return true;

            ItemStack a = itemStack.copy();
            ItemStack b = stack.copy();

            a.setCount(0);
            b.setCount(0);


            if(a.getItem().equals(b.getItem())) return true;
        } catch (Exception e) {
            return true;
        }

        return false;
    }


    JedisPoolConfig poolConfig = new JedisPoolConfig();
    JedisPool jedisPool = new JedisPool(poolConfig, "localhost");

    public void SaveItem(ItemStack itemStack) {


        try (Jedis jedis = jedisPool.getResource()) {
            LOGGER.info("Saving to RedisDB...");
//            jedis.del("Inventory");
//            jedis.hset("Inventory", hash);

            int itemAmount = itemStack.getCount();
            CompoundTag data = itemStack.serializeNBT();
            data.putInt("Count", itemAmount);


            jedis.set("Inventory-" + 0 + "/" + data.hashCode(), data.toString());
        } catch (Exception e) {
            LOGGER.info("RedisDB Exception: " + e.getMessage());
        }


    }

    public void UpdatedRedis() {

        HashMap<String, String> hash = new HashMap<>();

        int index = 0;
        for(ItemStack itemStack : items) {
            int itemAmount = itemStack.getCount();
            CompoundTag data = itemStack.serializeNBT();
            data.putInt("Count", itemAmount);

            hash.put("Slot-" + index++, data.toString());
        }

        try (Jedis jedis = jedisPool.getResource()) {
            LOGGER.info("Saving to RedisDB...");
//            jedis.del("Inventory");
            jedis.hset("Inventory", hash);
        } catch (Exception e) {
            LOGGER.info("RedisDB Exception: " + e.getMessage());
        }

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
