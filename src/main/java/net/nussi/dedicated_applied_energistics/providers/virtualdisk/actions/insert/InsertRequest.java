package net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.insert;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import net.minecraft.nbt.CompoundTag;
import net.nussi.dedicated_applied_energistics.providers.virtualdisk.actions.Request;

public class InsertRequest extends Request {
    public AEKey what;
    public long amount;
    public Actionable mode;

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
    protected void saveData(CompoundTag compoundTag) {
        compoundTag.put("what", what.toTagGeneric());
        compoundTag.putLong("amount", amount);
        compoundTag.putBoolean("simulate", mode == Actionable.SIMULATE);
    }

    @Override
    protected void loadData(CompoundTag compoundTag) {
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
