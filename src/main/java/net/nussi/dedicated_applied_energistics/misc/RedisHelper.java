package net.nussi.dedicated_applied_energistics.misc;

import appeng.api.storage.MEStorage;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;

import java.util.UUID;

public class RedisHelper {

    public static String getPath(MEStorage storage, InterDimensionalInterfaceBlockEntity blockEntity) {
        return blockEntity.getUuid() + "/" +
                blockEntity.getBlockPos().getX()  + "_" + blockEntity.getBlockPos().getY()  + "_" + blockEntity.getBlockPos().getZ() + "/" +
                storage.hashCode();
    }

}
