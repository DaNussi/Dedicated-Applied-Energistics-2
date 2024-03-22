package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.mojang.logging.LogUtils;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import org.slf4j.Logger;

import java.util.*;

public class InterDimensionalInterfaceCrafting implements ICraftingProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private String uuid = UUID.randomUUID().toString();
    private InterDimensionalInterfaceBlockEntity instance;

    public InterDimensionalInterfaceCrafting(InterDimensionalInterfaceBlockEntity instance) {
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
    public List<IPatternDetails> getAvailablePatterns() {
        return Collections.emptyList();
    }

    @Override
    public int getPatternPriority() {
        return 0;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public Set<AEKey> getEmitableItems() {
        return new HashSet<>();
    }
}
