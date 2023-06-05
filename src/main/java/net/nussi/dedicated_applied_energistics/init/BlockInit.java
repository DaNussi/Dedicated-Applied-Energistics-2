package net.nussi.dedicated_applied_energistics.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nussi.dedicated_applied_energistics.blocks.InterDimensionalInterfaceBlock;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics.MODID;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);


    // ========================================================== //


    public static final RegistryObject<Block> INTER_DIMENSIONAL_INTERFACE_BLOCK = registerBlock("inter_dimensional_interface",
            new InterDimensionalInterfaceBlock(BlockBehaviour.Properties.of(Material.GLASS).strength(0.5f)),
            new Item.Properties().tab(TabInit.MAIN_TAB));


    // ========================================================== //

    private static RegistryObject<Block> registerBlock(String name, Block block, Item.Properties properties) {
        RegistryObject<Block> blockRegistry = BlockInit.BLOCKS.register(name, () -> block);
        ItemInit.ITEMS.register(name, () -> new BlockItem(blockRegistry.get(), properties));
        return blockRegistry;
    }
}
