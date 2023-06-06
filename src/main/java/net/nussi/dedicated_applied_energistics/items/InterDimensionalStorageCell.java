package net.nussi.dedicated_applied_energistics.items;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.items.AEBaseItem;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.Item;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class InterDimensionalStorageCell extends AEBaseItem {
    private static final Logger LOGGER = LogUtils.getLogger();
    public InterDimensionalInterfaceBlockEntity blockEntity;
    public InterDimensionalStorageCell(Properties properties) {
        super(properties);
        LOGGER.info("Created IntDemStorageCell");
    }

}
