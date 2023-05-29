package net.nussi.dedicated_applied_energistics.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nussi.dedicated_applied_energistics.blocks.TestBlock;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics.MODID;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);


    // ========================================================== //

    public static final RegistryObject<Block> TEST_BLOCK = registerBlock("test_block",
            new TestBlock(BlockBehaviour.Properties.of(Material.STONE)),
            new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS));


    public static final RegistryObject<Block> EXAMPLE_BLOCK = registerBlock("example_block",
            new Block(BlockBehaviour.Properties.of(Material.STONE)),
            new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS));

    // ========================================================== //

    private static RegistryObject<Block> registerBlock(String name, Block block, Item.Properties properties) {
        RegistryObject<Block> blockRegistry = BlockInit.BLOCKS.register(name, () -> block);
        ItemInit.ITEMS.register(name, () -> new BlockItem(blockRegistry.get(), properties));
        return blockRegistry;
    }
}
