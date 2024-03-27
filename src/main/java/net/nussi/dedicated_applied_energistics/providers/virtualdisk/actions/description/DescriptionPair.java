package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.description;

import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.Pair;

import java.util.concurrent.CompletableFuture;

public class DescriptionPair extends Pair<DescriptionRequest, DescriptionResponse> {
    public DescriptionPair(DescriptionRequest request, CompletableFuture<DescriptionResponse> responseFuture) {
        super(request, responseFuture);
    }

    public DescriptionPair(DescriptionRequest request) {
        super(request);
    }
}
