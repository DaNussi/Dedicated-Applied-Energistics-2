package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public abstract class Compoundable implements Serializable {

    public Compoundable() {
    }

    public Compoundable(byte[] bytes) throws Exception {
        String data = new String(bytes, StandardCharsets.UTF_8);
        CompoundTag compoundTag = TagParser.parseTag(data);
        load(compoundTag);
    }

    public Compoundable(String data) throws Exception {
        CompoundTag compoundTag = TagParser.parseTag(data);
        load(compoundTag);
    }

    public byte[] toBytes() {
        CompoundTag compoundTag = new CompoundTag();
        save(compoundTag);
        String data = compoundTag.toString();
        return data.getBytes(StandardCharsets.UTF_8);
    }

    abstract void save(CompoundTag compoundTag);

    abstract void load(CompoundTag compoundTag);
}
