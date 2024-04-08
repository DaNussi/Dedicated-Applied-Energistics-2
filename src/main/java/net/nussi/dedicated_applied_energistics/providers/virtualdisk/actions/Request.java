package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions;

import net.minecraft.nbt.CompoundTag;

import java.io.Serializable;
import java.util.UUID;

public abstract class Request extends Compoundable {
    protected String id;

    public Request() {
        this.id = UUID.randomUUID().toString();
    }

    public Request(String id) {
        this.id = id;
    }

    public Request(byte[] bytes) throws Exception {
        super(bytes);
    }

    @Override
    void save(CompoundTag compoundTag) {
        compoundTag.putString("id", id);

        CompoundTag dataCompound = new CompoundTag();
        saveData(dataCompound);
        compoundTag.put("data", dataCompound);
    }

    @Override
    void load(CompoundTag compoundTag) {
        id = compoundTag.getString("id");

        CompoundTag dataCompound = compoundTag.getCompound("data");
        loadData(dataCompound);
    }

    protected abstract void saveData(CompoundTag compoundTag);

    protected abstract void loadData(CompoundTag compoundTag);

    public String getId() {
        return id;
    }
}
