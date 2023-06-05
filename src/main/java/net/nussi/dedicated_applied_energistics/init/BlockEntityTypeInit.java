package net.nussi.dedicated_applied_energistics.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics.MODID;

public class BlockEntityTypeInit {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    public static final RegistryObject<BlockEntityType<InterDimensionalInterfaceBlockEntity>> INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("inter_dimensional_interface", () -> BlockEntityType.Builder.of(InterDimensionalInterfaceBlockEntity::new, BlockInit.INTER_DIMENSIONAL_INTERFACE_BLOCK.get()).build(null));


}
