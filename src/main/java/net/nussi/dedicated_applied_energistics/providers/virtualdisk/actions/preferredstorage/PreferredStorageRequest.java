package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.preferredstorage;

import appeng.api.stacks.AEKey;
import net.minecraft.nbt.CompoundTag;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.Request;

public class PreferredStorageRequest extends Request {
    private AEKey data;

    public PreferredStorageRequest(AEKey data) {
        this.data = data;
    }

    public PreferredStorageRequest(byte[] bytes) throws Exception {
        super(bytes);
    }

    @Override
    protected void saveData(CompoundTag compoundTag) {
        compoundTag.put("what", data.toTagGeneric());
    }

    @Override
    protected void loadData(CompoundTag compoundTag) {
        data = AEKey.fromTagGeneric(compoundTag.getCompound("what"));
    }

    public AEKey getWhat() {
        return data;
    }
}
