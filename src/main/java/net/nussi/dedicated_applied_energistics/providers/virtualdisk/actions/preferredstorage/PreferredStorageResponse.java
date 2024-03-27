package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.preferredstorage;

import net.minecraft.nbt.CompoundTag;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.Response;

public class PreferredStorageResponse extends Response {
    private boolean data;

    public PreferredStorageResponse(String id, boolean success, boolean data) {
        super(id, success);
        this.data = data;
    }

    public PreferredStorageResponse(byte[] bytes) throws Exception {
        super(bytes);
    }

    @Override
    protected void saveData(CompoundTag compoundTag) {
        compoundTag.putBoolean("data", data);
    }

    @Override
    protected void loadData(CompoundTag compoundTag) {
        data = compoundTag.getBoolean("data");
    }

    public boolean getData() {
        return data;
    }
}
