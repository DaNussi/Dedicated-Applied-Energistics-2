package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.preferredstorage;

import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.Pair;

import java.util.concurrent.CompletableFuture;

public class PreferredStoragePair extends Pair<PreferredStorageRequest, PreferredStorageResponse> {
    public PreferredStoragePair(PreferredStorageRequest request, CompletableFuture<PreferredStorageResponse> responseFuture) {
        super(request, responseFuture);
    }

    public PreferredStoragePair(PreferredStorageRequest request) {
        super(request);
    }
}
