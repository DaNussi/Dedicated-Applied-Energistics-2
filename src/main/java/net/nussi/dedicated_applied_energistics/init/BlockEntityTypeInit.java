package net.nussi.dedicated_applied_energistics.init;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.BlockDefinition;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import net.nussi.dedicated_applied_energistics.blocks.InterDimensionalInterfaceBlock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics.MODID;

public class BlockEntityTypeInit {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

//    public static final RegistryObject<BlockEntityType<InterDimensionalInterfaceBlockEntity>> INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("inter_dimensional_interface", () ->
//            BlockEntityType.Builder
//                    .of(InterDimensionalInterfaceBlockEntity::new, BlockInit.INTER_DIMENSIONAL_INTERFACE_BLOCK.get())
//                    .build(null));

    public static final RegistryObject<BlockEntityType<InterDimensionalInterfaceBlockEntity>> INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("temp", () ->
            create(InterDimensionalInterfaceBlockEntity.class, BlockEntityType.Builder
                    .of(InterDimensionalInterfaceBlockEntity::new, BlockInit.INTER_DIMENSIONAL_INTERFACE_BLOCK.get())
                    .build(null), (AEBaseEntityBlock<InterDimensionalInterfaceBlockEntity>) BlockInit.INTER_DIMENSIONAL_INTERFACE_BLOCK.get()));


    public static <T extends AEBaseBlockEntity> BlockEntityType<T> create(Class<T> entityClass,
                                                                          BlockEntityType<T> type,
                                                                          AEBaseEntityBlock<T> block) {

        // If the block entity classes implement specific interfaces, automatically register them
        // as tickers with the blocks that create that entity.
        BlockEntityTicker<T> serverTicker = null;
        if (ServerTickingBlockEntity.class.isAssignableFrom(entityClass)) {
            serverTicker = (level, pos, state, entity) -> {
                ((ServerTickingBlockEntity) entity).serverTick();
            };
        }
        BlockEntityTicker<T> clientTicker = null;
        if (ClientTickingBlockEntity.class.isAssignableFrom(entityClass)) {
            clientTicker = (level, pos, state, entity) -> {
                ((ClientTickingBlockEntity) entity).clientTick();
            };
        }

        block.setBlockEntity(entityClass, type, clientTicker, serverTicker);

        return type;
    }


}
