package net.nussi.dedicated_applied_energistics.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nussi.dedicated_applied_energistics.blocks.InterDimensionalInterfaceBlock;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics.MODID;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    public static final RegistryObject<Block> INTER_DIMENSIONAL_INTERFACE_BLOCK = BLOCKS.register("inter_dimensional_interface", () ->
            new InterDimensionalInterfaceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(0.5f)));

}
