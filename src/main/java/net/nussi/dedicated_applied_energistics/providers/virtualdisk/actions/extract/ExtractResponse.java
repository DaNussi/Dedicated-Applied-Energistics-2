package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.extract;

import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert.InsertResponse;

public class ExtractResponse extends InsertResponse {
    public ExtractResponse(byte[] bytes) throws Exception {
        super(bytes);
    }

    public ExtractResponse(String id, boolean success, long data) {
        super(id, success, data);
    }
}
