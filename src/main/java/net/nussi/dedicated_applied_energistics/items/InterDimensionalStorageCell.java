package net.nussi.dedicated_applied_energistics.items;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.items.AEBaseItem;

import java.util.HashMap;
import java.util.Map;

public class InterDimensionalStorageCell extends AEBaseItem implements StorageCell {
    public InterDimensionalStorageCell(Properties properties) {
        super(properties);
    }

    @Override
    public CellState getStatus() {
        return CellState.EMPTY;
    }

    @Override
    public double getIdleDrain() {
        return 0;
    }

    @Override
    public void persist() {

    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return true;
    }


    HashMap<AEKey, Long> items = new HashMap<>();

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {

        if(items.containsKey(what)) {
            long a = items.get(what);
            a += amount;
            if(!mode.isSimulate()) items.put(what, a);
        } else {
            if(!mode.isSimulate()) items.put(what, amount);
        }

        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if(items.containsKey(what)) {
            long a = items.get(what);

            if(a > amount) {
                a -= amount;
                if(!mode.isSimulate()) items.put(what, a);
                return amount;
            } else if ( a == amount) {
                if(!mode.isSimulate()) items.remove(what);
                return amount;
            } else {
                if(!mode.isSimulate()) items.remove(what);
                return a;
            }
        } else {
            return 0;
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for(Map.Entry<AEKey, Long> pair : items.entrySet()) {
            out.add(pair.getKey(), pair.getValue());
        }
    }

}
