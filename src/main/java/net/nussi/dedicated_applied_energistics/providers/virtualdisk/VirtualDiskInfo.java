package net.nussi.dedicated_applied_energistics.providers.virtualdisk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

import java.nio.charset.StandardCharsets;

public class VirtualDiskInfo {
    public static final String REDIS_PATH = "VirtualDisks";

    private String origin;
    private int priority;

    public VirtualDiskInfo(String origin,int priority) {
        this.origin = origin;
        this.priority = priority;
    }

    public String getOrigin() {
        return origin;
    }

    public int getPriority() {
        return priority;
    }

    public byte[] toBytes() {
        return this.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("priority", priority);
        compoundTag.putString("origin", origin);

        return compoundTag.toString();
    }

    public static VirtualDiskInfo fromBytes(byte[] bytes) throws Exception {
        String data = new String(bytes, StandardCharsets.UTF_8);
        return fromString(data);
    }

    public static VirtualDiskInfo fromString(String data) throws Exception {
        CompoundTag compoundTag = TagParser.parseTag(data);

        return new VirtualDiskInfo(
                compoundTag.getString("origin"),
                compoundTag.getInt("priority")
        );
    }
}
