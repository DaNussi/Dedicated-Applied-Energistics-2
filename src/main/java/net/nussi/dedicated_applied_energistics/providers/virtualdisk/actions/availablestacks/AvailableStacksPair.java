package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.availablestacks;

import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.Pair;

import java.util.concurrent.CompletableFuture;

public class AvailableStacksPair extends Pair<AvailableStacksRequest, AvailableStacksResponse> {
    public AvailableStacksPair(AvailableStacksRequest request, CompletableFuture<AvailableStacksResponse> responseFuture) {
        super(request, responseFuture);
    }

    public AvailableStacksPair(AvailableStacksRequest request) {
        super(request);
    }
}
