package net.nussi.dedicated_applied_energistics.misc;

import appeng.api.storage.MEStorage;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;

public class RedisHelper {

    public static String getPath(MEStorage storage, InterDimensionalInterfaceBlockEntity blockEntity) {
        return DedicatedAppliedEnergisticsController.CONFIG_VALUE_HOST_ID.get() + "/" +
                blockEntity.getBlockPos().getX()  + "_" + blockEntity.getBlockPos().getY()  + "_" + blockEntity.getBlockPos().getZ() + "/" +
                storage.hashCode();
    }

}
