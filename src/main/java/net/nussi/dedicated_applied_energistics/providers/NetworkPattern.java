package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.PatternProviderBlockEntity;

import java.util.UUID;

public class NetworkPattern {
    public String patternUUID;
    public String hostUUID;
    public IPatternDetails pattern;
    public PatternProviderBlockEntity patternProvider;

    public NetworkPattern(String hostUUID, IPatternDetails pattern, PatternProviderBlockEntity patternProvider) {
        this.hostUUID = hostUUID;
        this.pattern = pattern;
        this.patternProvider = patternProvider;
        this.patternUUID = patternToUUID(pattern);
    }

    public boolean pushPattern(KeyCounter[] inputHolder) {

        return patternProvider.getLogic().pushPattern(pattern, inputHolder);
    }

        @Override
    public boolean equals(Object obj) {
        if (obj instanceof NetworkPattern networkPattern) {
            return networkPattern.patternUUID.equals(this.patternUUID);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NetworkPattern{" +
                "patternUUID='" + patternUUID + '\'' +
                ", hostUUID='" + hostUUID + '\'' +
                ", pattern=" + pattern +
                ", patternProvider=" + patternProvider +
                '}';
    }

    public static String patternToUUID(IPatternDetails pattern) {
        return String.valueOf(pattern.hashCode());
    }
}
