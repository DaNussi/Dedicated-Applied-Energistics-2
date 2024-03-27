package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.extract;

import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.Pair;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert.InsertPair;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert.InsertRequest;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert.InsertResponse;

import java.util.concurrent.CompletableFuture;

public class ExtractPair extends Pair<ExtractRequest, ExtractResponse> {

    public ExtractPair(ExtractRequest request, CompletableFuture<ExtractResponse> responseFuture) {
        super(request, responseFuture);
    }

    public ExtractPair(ExtractRequest request) {
        super(request);
    }
}
