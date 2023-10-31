package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.iface.PatternProviderLogic;
import appeng.helpers.iface.PatternProviderLogicHost;
import appeng.util.inv.AppEngInternalInventory;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class InterDimensionalInterfacePatternProvider extends PatternProviderLogic {
    private static final Logger LOGGER = LogUtils.getLogger();
    IManagedGridNode mainNode;
    List<IPatternDetails> patterns = new ArrayList<>();

    public InterDimensionalInterfacePatternProvider(IManagedGridNode mainNode, PatternProviderLogicHost host) {
        super(mainNode, host);
        this.mainNode = mainNode;
    }

    @Override
    public InternalInventory getPatternInv() {
        return new AppEngInternalInventory(this, 0);
    }


    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return patterns;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        return super.pushPattern(patternDetails, inputHolder);
    }

    @Override
    public void updatePatterns() {
        patterns.clear();

        ICraftingProvider.requestUpdate(mainNode);
    }
}
