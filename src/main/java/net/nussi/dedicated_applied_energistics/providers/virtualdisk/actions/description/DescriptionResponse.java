package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.description;

import net.minecraft.nbt.CompoundTag;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.Response;

public class DescriptionResponse extends Response {
    String description;

    public DescriptionResponse(byte[] bytes) throws Exception {
        super(bytes);
    }

    public DescriptionResponse(String id, boolean success, String description) {
        super(id, success);
        this.description = description;
    }

    @Override
    protected void saveData(CompoundTag compoundTag) {
        compoundTag.putString("description", description);
    }

    @Override
    protected void loadData(CompoundTag compoundTag) {
        this.description = compoundTag.getString("description");
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "DescriptionResponse{" +
                "description='" + description + '\'' +
                ", id='" + id + '\'' +
                ", success=" + success +
                '}';
    }
}
