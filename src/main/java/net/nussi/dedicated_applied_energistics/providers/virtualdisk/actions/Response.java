package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions;

import net.minecraft.nbt.CompoundTag;

public abstract class Response extends Compoundable {
    protected String id;
    protected boolean success;

    public Response(String id, boolean success) {
        this.id = id;
        this.success = success;
    }

    public Response(byte[] bytes) throws Exception {
        super(bytes);
    }

    @Override
    void save(CompoundTag compoundTag) {
        compoundTag.putString("id", id);
        compoundTag.putBoolean("success", success);

        CompoundTag dataCompound = new CompoundTag();
        saveData(dataCompound);
        compoundTag.put("data", dataCompound);
    }

    @Override
    void load(CompoundTag compoundTag) {
        id = compoundTag.getString("id");
        success = compoundTag.getBoolean("success");

        CompoundTag dataCompound = compoundTag.getCompound("data");
        loadData(dataCompound);
    }

    protected abstract void saveData(CompoundTag compoundTag);

    protected abstract void loadData(CompoundTag compoundTag);

    public String getId() {
        return id;
    }

    public boolean isSuccess() {
        return success;
    }
}
