package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.extract;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert.InsertRequest;

public class ExtractRequest extends InsertRequest {
    public ExtractRequest(AEKey what, long amount, Actionable mode) {
        super(what, amount, mode);
    }

    public ExtractRequest(byte[] bytes) throws Exception {
        super(bytes);
    }
}
