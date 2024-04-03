package net.nussi.dedicated_applied_energistics.providers.virtualdisk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

import java.nio.charset.StandardCharsets;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController.CONFIG_VALUE_HOST_IP;
import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController.CONFIG_VALUE_HOST_PORT;

public class VirtualDiskInfo {
    public static final String REDIS_PATH = "VirtualDisks";

    private String origin;
    private int priority;
    private String host_ip;
    private int host_port;

    public VirtualDiskInfo(String origin,int priority) {
        this.origin = origin;
        this.priority = priority;
        this.host_ip = CONFIG_VALUE_HOST_IP.get();
        this.host_port = CONFIG_VALUE_HOST_PORT.get();
    }

    public VirtualDiskInfo(String origin, int priority, String host_ip, int host_port) {
        this.origin = origin;
        this.priority = priority;
        this.host_ip = host_ip;
        this.host_port = host_port;
    }

    public String getOrigin() {
        return origin;
    }

    public int getPriority() {
        return priority;
    }

    public String getHostIp() {
        return host_ip;
    }

    public int getHostPort() {
        return host_port;
    }

    public byte[] toBytes() {
        return this.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("priority", priority);
        compoundTag.putString("origin", origin);
        compoundTag.putString("host_ip", host_ip);
        compoundTag.putInt("host_port", host_port);

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
                compoundTag.getInt("priority"),
                compoundTag.getString("host_ip"),
                compoundTag.getInt("host_port")
        );
    }
}
