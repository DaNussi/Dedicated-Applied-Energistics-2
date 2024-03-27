package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.availablestacks;

import net.minecraft.nbt.CompoundTag;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.Request;

public class AvailableStacksRequest extends Request {

    public AvailableStacksRequest() {
    }

    public AvailableStacksRequest(String id) {
        super(id);
    }

    public AvailableStacksRequest(byte[] bytes) throws Exception {
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
        return "AvailableStacksRequest{" +
                "id='" + id + '\'' +
                '}';
    }
}
