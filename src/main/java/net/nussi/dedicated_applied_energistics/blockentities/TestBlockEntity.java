package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.AEBaseInvBlockEntity;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class TestBlockEntity extends AEBaseInvBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final InternalInventory internalInventory = new DedicatedInternalInventory();

    public TestBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.TEST_ENTITY_TYPE.get(), pos, state);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return internalInventory;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        // THIS METHOD IS BROKEN AN SHOULD NOT BE USED
//        LOGGER.info("onChangeInventory " + slot + " " + inv.getSlotInv(slot));
    }


    @Override
    public void saveAdditional(CompoundTag data) {
        // No Saving
    }

    @Override
    public void loadTag(CompoundTag data) {
        // No Loading
    }

    public static class DedicatedInternalInventory extends BaseInternalInventory {
        JedisPool pool = new JedisPool("localhost", 6379);
        Jedis jedis = pool.getResource();

        public String getRedisIndex(int slotIndex) {
            StringBuilder builder = new StringBuilder();

            builder.append("0");
            builder.append(".inv");
            builder.append("/");
            builder.append(slotIndex);
            builder.append(".slot");

            return builder.toString();
        }



        @Override
        public int size() {
            return Math.toIntExact(jedis.dbSize()) + 1;
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            String redisIndex = getRedisIndex(slotIndex);

            if(!jedis.exists(redisIndex)) return ItemStack.EMPTY;

            try {
                Stopwatch stopwatch = Stopwatch.createStarted();
                stopwatch.reset();
                stopwatch.start();
                String data = jedis.get(redisIndex);
                stopwatch.stop();
                LOGGER.info("Retrieve data took: " + stopwatch.elapsed());

                CompoundTag compoundTag = TagParser.parseTag(data);
                ItemStack out = ItemStack.of(compoundTag);
                out.setCount(compoundTag.getInt("Count"));
                return out;
            } catch (CommandSyntaxException e) {
                return ItemStack.EMPTY;
            }
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            String redisIndex = getRedisIndex(slotIndex);

            if(stack.getCount() == 0) {
                if(!jedis.exists(redisIndex)) return;
                jedis.del(redisIndex);
                return;
            }

            CompoundTag compoundTag = stack.serializeNBT();

            compoundTag.remove("Count");
            compoundTag.putInt("Count", stack.getCount());


            String data = compoundTag.getAsString();
            jedis.set(redisIndex, data);
//            LOGGER.info("Stack: " + slotIndex + " " + stack.getItem() + " x " + stack.getCount());
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
