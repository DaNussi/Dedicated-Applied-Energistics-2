package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.AEBaseInvBlockEntity;
import com.google.common.base.Preconditions;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.Hashtable;
import java.util.UUID;

public class InterDimensionalInterfaceBlockEntity extends AEBaseInvBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DedicatedInternalInventory internalInventory = new DedicatedInternalInventory("localhost", 6379, 0);

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get(), pos, state);
        LOGGER.info("CREATING ENTITY!");
    }

    @Override
    public InternalInventory getInternalInventory() {
        return internalInventory;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
//        LOGGER.info("onChangeInventory " + slot + " " + inv.getSlotInv(slot));
    }


    @Override
    public void saveAdditional(CompoundTag data) {
        // No Saving
        LOGGER.info("SAVING ENTITY!");
    }

    @Override
    public void loadTag(CompoundTag data) {
        // No Loading
        LOGGER.info("LOADING ENTITY!");
    }

    public void onBlockBreak() {
        try {
            internalInventory.sendJedis.close();
            internalInventory.reciveJedis.close();
            internalInventory.reciveSub.unsubscribe(internalInventory.channel);
            internalInventory.pool.close();
        } catch (Exception e) {}
    }

    public static class DedicatedInternalInventory extends BaseInternalInventory {
        public Hashtable<Integer, ItemStack> internalInventory = new Hashtable<>();
        public JedisPool pool;
        public Jedis sendJedis;
        public Jedis reciveJedis;
        public JedisPubSub reciveSub;
        public String channel;
        public String uuid;
        public Thread thread;

        public DedicatedInternalInventory(String host, int port, int inventory) {
            pool = new JedisPool(host, port);
            sendJedis = pool.getResource();
            reciveJedis = pool.getResource();

            channel = inventory + ".inv";
            uuid = UUID.randomUUID().toString();

            for (String redisIndex : reciveJedis.keys(channel+"/*.slot")) {
                if(!reciveJedis.exists(redisIndex)) continue;
                String data = reciveJedis.get(redisIndex);
                downloadItem(data);
            }

            thread = new Thread(() -> {
                reciveSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        downloadItem(message);
                    }
                };

                reciveJedis.subscribe(reciveSub, channel);
            });
            thread.start();
        }


        @Override
        public int size() {
            return internalInventory.size() + 1;
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            if(!internalInventory.containsKey(slotIndex)) return ItemStack.EMPTY;
            return internalInventory.get(slotIndex);
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            if(stack.getCount() == 0) {
//                internalInventory.remove(slotIndex);
                internalInventory.put(slotIndex, ItemStack.EMPTY);
            } else {
                internalInventory.put(slotIndex, stack);
            }


            // Upload Item
            uploadItem(slotIndex, stack);
        }

        public void uploadItem(int slotIndex, ItemStack stack) {
            CompoundTag data = new CompoundTag();
            CompoundTag itemData = serializeItem(stack);
            if(stack.getCount() == 0) {
                data.putBoolean("Delete", true);
            } else {
                data.put("Item", itemData);
            }
            data.putInt("Slot", slotIndex);
            data.putString("UUID", uuid);

            LOGGER.info("Sent["+this.uuid+"] " + stack);
            sendJedis.publish(channel, data.getAsString());

            // REDIS
            String redisIndex = getRedisIndex(slotIndex);
            sendJedis.set(redisIndex, data.getAsString());
        }

        public void downloadItem(String text) {
            try {
                CompoundTag data = TagParser.parseTag(text);
                String uuid = data.getString("UUID");
                if(uuid.equals(this.uuid)) return;

                int slotIndex = data.getInt("Slot");

                if(data.contains("Delete")) {
                    internalInventory.put(slotIndex, ItemStack.EMPTY);
                    LOGGER.info("Received["+this.uuid+"] " + ItemStack.EMPTY);
                    return;
                }

                Tag itemCompoundTag = data.get("Item");
                ItemStack itemStack = deserializeItem((CompoundTag) itemCompoundTag);

                LOGGER.info("Received["+this.uuid+"] " + itemStack);
                internalInventory.put(slotIndex, itemStack);
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        /** Can serialize ItemStacks larger then 64 **/
        public CompoundTag serializeItem(ItemStack itemStack) {
            CompoundTag compoundTag = itemStack.serializeNBT();

            compoundTag.remove("Count"); // Not sure if I need this C:
            compoundTag.putInt("Count", itemStack.getCount());

            return compoundTag;
        }

        /** Can deserialize ItemStacks larger then 64 **/
        public ItemStack deserializeItem(CompoundTag compoundTag) {
            try {
                ItemStack out = ItemStack.of(compoundTag);
                out.setCount(compoundTag.getInt("Count"));
                return out;
            } catch (Exception e) {
                return ItemStack.EMPTY;
            }
        }

        public String getRedisIndex(int slotIndex) {
            StringBuilder builder = new StringBuilder();

            builder.append("0");
            builder.append(".inv");
            builder.append("/");
            builder.append(slotIndex);
            builder.append(".slot");

            return builder.toString();
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
