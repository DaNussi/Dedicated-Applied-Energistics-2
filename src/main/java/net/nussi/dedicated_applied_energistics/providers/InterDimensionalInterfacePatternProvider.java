package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.iface.PatternProviderLogic;
import appeng.helpers.iface.PatternProviderLogicHost;
import appeng.util.inv.AppEngInternalInventory;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.List;

public class InterDimensionalInterfacePatternProvider extends PatternProviderLogic {
    private static final Logger LOGGER = LogUtils.getLogger();

    public InterDimensionalInterfacePatternProvider(IManagedGridNode mainNode, PatternProviderLogicHost host) {
        super(mainNode, host);
    }

    @Override
    public InternalInventory getPatternInv() {
        return new AppEngInternalInventory(this, 0);
    }


    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return super.getAvailablePatterns();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        return super.pushPattern(patternDetails, inputHolder);
    }


}
