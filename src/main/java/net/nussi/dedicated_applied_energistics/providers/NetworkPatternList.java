package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.crafting.IPatternDetails;

import java.util.List;
import java.util.Optional;
import java.util.Vector;

public class NetworkPatternList {
    private static final Vector<NetworkPattern> patterns = new Vector<>();
    private String hostUUID;

    public NetworkPatternList(String hostUUID) {
        this.hostUUID = hostUUID;
    }

    public List<NetworkPattern> getLocalNetworkPatterns() {
        return patterns.stream().filter(networkPattern -> networkPattern.hostUUID.equals(this.hostUUID)).toList();
    }

    public List<NetworkPattern> getRemoteNetworkPatterns() {
        return patterns.stream().filter(networkPattern -> !networkPattern.hostUUID.equals(this.hostUUID)).toList();
    }

    public List<IPatternDetails> getLocalPatterns() {
        return getLocalNetworkPatterns().stream().map(networkPattern -> networkPattern.pattern).toList();
    }

    public List<IPatternDetails> getRemotePatterns() {
        return getRemoteNetworkPatterns().stream().map(networkPattern -> networkPattern.pattern).toList();
    }

    public Vector<NetworkPattern> instance() {
        return patterns;
    }

    public void add(NetworkPattern networkPattern) {
        patterns.add(networkPattern);
    }

    public void remove(NetworkPattern networkPattern) {
        patterns.remove(networkPattern);
    }

    public NetworkPattern getByPattern(IPatternDetails pattern) {
        for(NetworkPattern networkPattern : patterns) {
            if(networkPattern.patternUUID.equals(NetworkPattern.patternToUUID(pattern))) return networkPattern;
        }
        return null;
    }

}
