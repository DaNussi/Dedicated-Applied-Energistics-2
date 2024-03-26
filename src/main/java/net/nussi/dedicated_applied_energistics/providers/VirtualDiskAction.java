package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class VirtualDiskAction {
    private static final Logger LOGGER = LogUtils.getLogger();


    public static class Insert {
        String id;
        AEKey what;
        long amount;
        Actionable mode;

        public Insert(AEKey what, long amount, Actionable mode) {
            this.id = UUID.randomUUID().toString();
            this.what = what;
            this.amount = amount;
            this.mode = mode;
        }

        public Insert(byte[] bytes) {
            try {
                String data = new String(bytes, StandardCharsets.UTF_8);
                CompoundTag compoundTag = TagParser.parseTag(data);

                this.id = compoundTag.getString("id");
                this.what = AEKey.fromTagGeneric(compoundTag.getCompound("what"));
                this.amount = compoundTag.getLong("amount");
                this.mode = compoundTag.getBoolean("simulate") ? Actionable.SIMULATE : Actionable.MODULATE;
            } catch (Exception e) {
                LOGGER.error("Failed to parse insert action");
                e.printStackTrace();
            }
        }

        public byte[] getData() {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("id", id);
            compoundTag.put("what", what.toTagGeneric());
            compoundTag.putLong("amount", amount);
            compoundTag.putBoolean("simulate", mode.isSimulate());
            return compoundTag.toString().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String toString() {
            return "Insert{" +
                    "id='" + id + '\'' +
                    ", what=" + what +
                    ", amount=" + amount +
                    ", mode=" + mode +
                    '}';
        }
    }

    public static class InsertResponse {
        String id;
        boolean success;
        long data;

        public InsertResponse(String id, boolean success, long data) {
            this.id = id;
            this.success = success;
            this.data = data;
        }

        public InsertResponse(byte[] bytes) {
            try {
                String data = new String(bytes, StandardCharsets.UTF_8);
                CompoundTag compoundTag = TagParser.parseTag(data);
                this.id = compoundTag.getString("id");
                this.data = compoundTag.getLong("data");
                this.success = compoundTag.getBoolean("success");
            } catch (Exception e) {
                LOGGER.error("Failed to parse insert response");
                e.printStackTrace();
            }
        }

        public byte[] getData() {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("id", id);
            compoundTag.putBoolean("success", success);
            compoundTag.putLong("data", data);
            return compoundTag.toString().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String toString() {
            return "InsertResponse{" +
                    "id='" + id + '\'' +
                    ", success=" + success +
                    ", data=" + data +
                    '}';
        }
    }

    public static class Extract extends Insert {
        public Extract(AEKey what, long amount, Actionable mode) {
            super(what, amount, mode);
        }

        public Extract(byte[] bytes) throws Exception {
            super(bytes);
        }
    }


}
