package net.nussi.dedicated_applied_energistics.providers.virtualdisk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

import java.nio.charset.StandardCharsets;

public class VirtualDiskInfo {
    public static final String REDIS_PATH = "VirtualDisks";

    private String origin;

    private String insertMethodID;
    private String extractMethodID;
    private String availableStacksMethodID;
    private String descriptionMethodID;
    private String preferredStorageMethodID;


    public VirtualDiskInfo(String origin, String insertMethodID, String extractMethodID, String availableStacksMethodID, String descriptionMethodID, String preferredStorageMethodID) {
        this.origin = origin;
        this.insertMethodID = insertMethodID;
        this.extractMethodID = extractMethodID;
        this.availableStacksMethodID = availableStacksMethodID;
        this.descriptionMethodID = descriptionMethodID;
        this.preferredStorageMethodID = preferredStorageMethodID;
    }

    public String getOrigin() {
        return origin;
    }


    public byte[] toBytes() {
        return this.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("origin", origin);
        compoundTag.putString("insertMethodID", insertMethodID);
        compoundTag.putString("extractMethodID", extractMethodID);
        compoundTag.putString("availableStacksMethodID", availableStacksMethodID);
        compoundTag.putString("descriptionMethodID", descriptionMethodID);
        compoundTag.putString("preferredStorageMethodID", preferredStorageMethodID);

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
                compoundTag.getString("insertMethodID"),
                compoundTag.getString("extractMethodID"),
                compoundTag.getString("availableStacksMethodID"),
                compoundTag.getString("descriptionMethodID"),
                compoundTag.getString("preferredStorageMethodID")
        );
    }

    public String getInsertMethodID() {
        return insertMethodID;
    }

    public String getExtractMethodID() {
        return extractMethodID;
    }

    public String getAvailableStacksMethodID() {
        return availableStacksMethodID;
    }

    public String getDescriptionMethodID() {
        return descriptionMethodID;
    }

    public String getPreferredStorageMethodID() {
        return preferredStorageMethodID;
    }
}
