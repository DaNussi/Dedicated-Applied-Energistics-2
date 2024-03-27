package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert;

import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.Pair;

import java.util.concurrent.CompletableFuture;

public class InsertPair extends Pair<InsertRequest, InsertResponse> {
    public InsertPair(InsertRequest request) {
        super(request);
    }

    public InsertPair(InsertRequest request, CompletableFuture<InsertResponse> responseFuture) {
        super(request, responseFuture);
    }
}
