package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert;

import net.minecraft.nbt.CompoundTag;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.Response;

public class InsertResponse extends Response {
    public long data;

    public InsertResponse(byte[] bytes) throws Exception {
        super(bytes);
    }

    public InsertResponse(String id, boolean success, long data) {
        super(id, success);
        this.data = data;
    }

    @Override
    protected void saveData(CompoundTag compoundTag) {
        compoundTag.putLong("data", data);
    }

    @Override
    protected void loadData(CompoundTag compoundTag) {
        this.data = compoundTag.getLong("data");
    }

    @Override
    public String toString() {
        return "InsertResponse{" +
                "data=" + data +
                ", id='" + id + '\'' +
                ", success=" + success +
                '}';
    }
}
