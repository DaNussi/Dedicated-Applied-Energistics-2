package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.availablestacks;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.Response;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class AvailableStacksResponse extends Response {
    public KeyCounter keyCounter;

    public AvailableStacksResponse(byte[] bytes) throws Exception {
        super(bytes);
    }

    public AvailableStacksResponse(String id, boolean success) {
        super(id, success);
    }

    public AvailableStacksResponse(String id, boolean success, KeyCounter keyCounter) {
        super(id, success);
        this.keyCounter = keyCounter;
    }

    @Override
    protected void saveData(CompoundTag compoundTag) {
        if (keyCounter == null) {
            compoundTag.putInt("size", 0);
            return;
        }
        compoundTag.putInt("size", keyCounter.size());

        CompoundTag whatCompound = new CompoundTag();
        CompoundTag amountCompound = new CompoundTag();
        AtomicInteger index = new AtomicInteger();
        keyCounter.keySet().forEach(what -> {
            int i = index.getAndIncrement();
            whatCompound.put(String.valueOf(i), what.toTagGeneric());
            amountCompound.putLong(String.valueOf(i), keyCounter.get(what));
        });
        compoundTag.put("what", whatCompound);
        compoundTag.put("amount", amountCompound);
    }

    @Override
    protected void loadData(CompoundTag compoundTag) {
        int size = compoundTag.getInt("size");
        CompoundTag whatCompound = compoundTag.getCompound("what");
        CompoundTag amountCompound = compoundTag.getCompound("amount");

        keyCounter = new KeyCounter();
        keyCounter.clear();
        for (int i = 0; i < size; i++) {
            AEKey what = AEKey.fromTagGeneric(whatCompound.getCompound(String.valueOf(i)));
            long amount = amountCompound.getLong(String.valueOf(i));
            keyCounter.add(what, amount);
        }
    }

    public KeyCounter getAvailableStacks() {
        return keyCounter;
    }

    public void getAvailableStacks(KeyCounter out) {
        keyCounter.keySet().forEach(key -> {
            out.add(key, keyCounter.get(key));
        });
    }

    @Override
    public String toString() {
        return "AvailableStacksResponse{" +
                "keyCounter=" + keyCounter.keySet().stream().map(Object::toString).toList() +
                ", id='" + id + '\'' +
                ", success=" + success +
                '}';
    }
}
