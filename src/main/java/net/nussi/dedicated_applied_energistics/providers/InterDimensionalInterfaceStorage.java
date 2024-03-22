package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import org.slf4j.Logger;

import java.util.Hashtable;
import java.util.Map;

public class InterDimensionalInterfaceStorage implements IStorageProvider, MEStorage {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Hashtable<AEKey, Long> localHashMap = new Hashtable<>();
    private String uuid = java.util.UUID.randomUUID().toString();
    private InterDimensionalInterfaceBlockEntity instance;

    public InterDimensionalInterfaceStorage(InterDimensionalInterfaceBlockEntity instance) {
        this.instance = instance;
    }

    public void onStart() {
        LOGGER.info(uuid + " | Starting storage provider!");

        LOGGER.info(uuid + " | Started storage provider!");
    }

    public void onStop() {
        LOGGER.info(uuid + " | Stopping storage provider!");

        LOGGER.info(uuid + " | Stopped storage provider!");
    }


    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return 0;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return 0;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        Hashtable<AEKey, Long> temp = (Hashtable<AEKey, Long>) localHashMap.clone();

        for(Map.Entry<AEKey, Long>  pair : temp.entrySet()) {
            out.add(pair.getKey(), pair.getValue());
        }
    }

    @Override
    public Component getDescription() {
        return Component.literal("Inter Dimensional Interface Storage");
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        storageMounts.mount(this);
    }
}
