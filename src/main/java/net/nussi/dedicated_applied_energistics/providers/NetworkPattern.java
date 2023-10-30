package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.crafting.IPatternDetails;

public class NetworkPattern {
    private String hostUUID;

    public NetworkPattern(String hostUUID, IPatternDetails pattern) {
        this.hostUUID = hostUUID;
    }
}
