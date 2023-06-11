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
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.blockentity.inventory.AppEngCellInventory;
import appeng.me.storage.DriveWatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import net.nussi.dedicated_applied_energistics.init.ItemInit;
import net.nussi.dedicated_applied_energistics.items.InterDimensionalStorageCell;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;


public class InterDimensionalInterfaceBlockEntity extends AENetworkInvBlockEntity implements IStorageProvider, StorageCell {

    private static final Logger LOGGER = LogUtils.getLogger();
    AppEngCellInventory inv = new AppEngCellInventory(this, 1);
    DriveWatcher driveWatcher;

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get(), pos, state);


    }

    public void onCreate() {
        redisInit();


        this.getMainNode()
                .addService(IStorageProvider.class, this)
                .setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    public void onDestroy() {
        redisReset();

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

    @Override
    public Component getDescription() {
        return Component.literal("Inter Dimensional Interface");
    }

    JedisPool jedisPool = new JedisPool();
    Jedis jedis;
    Jedis reciveJedis;
    HashMap<AEKey, Long> localHashMap = new HashMap<>();
    int inventoryIndex = 0;
    String UUID = java.util.UUID.randomUUID().toString();
    Thread thread;
    JedisPubSub pubSub;

    public void redisInit() {
        jedisPool = new JedisPool("localhost", 6379);
        jedis = jedisPool.getResource();
        reciveJedis = jedisPool.getResource();
//        LOGGER.info("Created 2 connections!");

        this.redisFetch();
        this.redisSubscriber();
    }

    public void redisReset() {
        if(this.jedis != null) {
            this.jedis.disconnect();
            this.jedis.close();
            this.jedis = null;
        }

        if(this.pubSub != null) {
            this.pubSub.unsubscribe(redisChannel());
            this.pubSub.unsubscribe();
            this.pubSub = null;
        }

        if(this.reciveJedis != null) {
            this.reciveJedis.disconnect();
            this.reciveJedis.close();
            this.reciveJedis = null;
        }

        if(this.thread != null) {
            try {
                this.thread.join();
                this.thread = null;
            } catch (Exception e) {}
        }

        if(this.jedisPool != null) {
            this.jedisPool.close();
            this.jedisPool.destroy();
            this.jedisPool = null;
        }

//        LOGGER.info("Removed 2 connections!");
    }

    public void redisFetch() {
        Set<String> keys = jedis.keys(redisChannel()+"/*.item");
        long maxCount = keys.size();
        int counter = 1;

        LOGGER.info("Fetching Inventory ...");
        for(String key : keys) {
            System.out.print("\rFetching Inventory [" + counter++ + "/" + maxCount + "]");

            try {
                String data = jedis.get(key);
                CompoundTag compoundTag = TagParser.parseTag(data);

                long amount = compoundTag.getLong("Amount");
                CompoundTag itemTag = compoundTag.getCompound("Item");
                AEKey what = AEKey.fromTagGeneric(itemTag);

                localHashMap.put(what, amount);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        System.out.println();
    }

    public void redisSubscriber() {
        this.thread = new Thread(() -> {
            try {
                pubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        try {
                            CompoundTag compoundTag = TagParser.parseTag(message);
                            String msgUUID = compoundTag.getString("UUID");
                            if(msgUUID.equals(UUID)) return;

                            long amount = compoundTag.getLong("Amount");
                            CompoundTag itemTag = compoundTag.getCompound("Item");
                            AEKey what = AEKey.fromTagGeneric(itemTag);

                            offset(what, amount, Actionable.MODULATE, true);
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                };

                reciveJedis.subscribe(pubSub, redisChannel());
            } catch (Exception e) {}
        });
        this.thread.start();
    }

    public String redisIndex(AEKey what) {
        return redisChannel() + "/" + itemKey(what) + ".item";
    }

    public String redisChannel() {
        return inventoryIndex + ".inv";
    }

    public static String itemKey(AEKey what) {
        return String.valueOf(what.toTagGeneric().toString().hashCode());
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        offset(what, amount, mode, false);
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if(!localHashMap.containsKey(what)) return 0;

        long currentAmount = localHashMap.get(what);
        if (amount <= currentAmount) {
            offset(what, -amount, mode, false);
            return amount;
        } else {
            long newAmount = amount - (amount - currentAmount);
            offset(what, -newAmount, mode, false);
            return newAmount;
        }
    }

    public void offset(AEKey what, long amount, Actionable mode, boolean fromMsg) {
        long newAmount = amount;

        if(localHashMap.containsKey(what)) {
            long currentAmount = localHashMap.get(what);
            newAmount += currentAmount;
//            LOGGER.info("CURRENT " + what.getDisplayName().toString() + " " + newAmount + " " + localHashMap.size());
        }

        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putLong("Amount", amount);
        compoundTag.put("Item", what.toTagGeneric());
        compoundTag.putString("UUID", UUID);
        compoundTag.putString("Index", redisIndex(what));

        if(mode != Actionable.SIMULATE) {
            if(!fromMsg) jedis.publish(redisChannel(), compoundTag.toString());

            if(newAmount > 0) {
                localHashMap.put(what, newAmount);
//                LOGGER.info("PUT " + what.getDisplayName().toString() + " " + newAmount + " " + localHashMap.size());
            } else {
                localHashMap.remove(what);
//                LOGGER.info("REMOVE " + what.getDisplayName().toString() + " " + 0 + " " + localHashMap.size());
            }
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for(Map.Entry<AEKey, Long> pair : localHashMap.entrySet()) {
            out.add(pair.getKey(), pair.getValue());
        }
    }

    public void onBlockBreak() {
        this.redisReset();
    }
}
