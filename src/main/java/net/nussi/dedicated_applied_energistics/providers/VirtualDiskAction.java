package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VirtualDiskAction {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static class InsertRequest extends Request {
        AEKey what;
        long amount;
        Actionable mode;

        public InsertRequest(AEKey what, long amount, Actionable mode) {
            super();
            this.what = what;
            this.amount = amount;
            this.mode = mode;
        }

        public InsertRequest(byte[] bytes) throws Exception {
            super(bytes);
        }

        @Override
        void saveData(CompoundTag compoundTag) {
            compoundTag.put("what", what.toTagGeneric());
            compoundTag.putLong("amount", amount);
            compoundTag.putBoolean("simulate", mode == Actionable.SIMULATE);
        }

        @Override
        void loadData(CompoundTag compoundTag) {
            this.what = AEKey.fromTagGeneric(compoundTag.getCompound("what"));
            this.amount = compoundTag.getLong("amount");
            this.mode = compoundTag.getBoolean("simulate") ? Actionable.SIMULATE : Actionable.MODULATE;
        }

        @Override
        public String toString() {
            return "InsertRequest{" +
                    "what=" + what +
                    ", amount=" + amount +
                    ", mode=" + mode +
                    ", id='" + id + '\'' +
                    '}';
        }
    }

    public static class InsertResponse extends Response {
        long data;

        public InsertResponse(byte[] bytes) throws Exception {
            super(bytes);
        }

        public InsertResponse(String id, boolean success, long data) {
            super(id, success);
            this.data = data;
        }

        @Override
        void saveData(CompoundTag compoundTag) {
            compoundTag.putLong("data", data);
        }

        @Override
        void loadData(CompoundTag compoundTag) {
            this.data = compoundTag.getLong("data");
        }

        @Override
        public String toString() {
            return "InsertResponse{" +
                    "data=" + data +
                    ", id='" + id + '\'' +
                    ", success=" + success +
                    '}';
        }
    }

    public static class InsertPair extends Pair<InsertRequest, InsertResponse>{
        public InsertPair(InsertRequest request) {
            super(request);
        }
    }

    public abstract static class Compoundable {

        public Compoundable(){}

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

    public abstract static class Request extends Compoundable {
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

        abstract void saveData(CompoundTag compoundTag);
        abstract void loadData(CompoundTag compoundTag);

        public String getId() {
            return id;
        }
    }

    public abstract static class Response extends Compoundable {
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

        abstract void saveData(CompoundTag compoundTag);
        abstract void loadData(CompoundTag compoundTag);

        public String getId() {
            return id;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    public static class Pair<T extends Request, N extends Response> {
        T request;
        CompletableFuture<N> responseFuture;

        public Pair(T request, CompletableFuture<N> responseFuture) {
            this.request = request;
            this.responseFuture = responseFuture;
        }

        public Pair(T request) {
            this.request = request;
            this.responseFuture = new CompletableFuture<>();
        }

        public T getRequest() {
            return request;
        }

        public CompletableFuture<N> getResponseFuture() {
            return responseFuture;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair<?, ?> pair)) return false;
            return Objects.equals(request, pair.request) && Objects.equals(responseFuture, pair.responseFuture);
        }

        @Override
        public int hashCode() {
            return Objects.hash(request, responseFuture);
        }

    }


}
