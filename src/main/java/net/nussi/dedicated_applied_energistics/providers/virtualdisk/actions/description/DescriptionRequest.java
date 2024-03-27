package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.description;

import net.minecraft.nbt.CompoundTag;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.Request;

public class DescriptionRequest extends Request {
    public DescriptionRequest() {
    }

    public DescriptionRequest(String id) {
        super(id);
    }

    public DescriptionRequest(byte[] bytes) throws Exception {
        super(bytes);
    }

    @Override
    protected void saveData(CompoundTag compoundTag) {

    }

    @Override
    protected void loadData(CompoundTag compoundTag) {

    }

    @Override
    public String toString() {
        return "DescriptionRequest{" +
                "id='" + id + '\'' +
                '}';
    }
}
