package net.nussi.dedicated_applied_energistics.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics.MODID;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> INTER_DIMENSIONAL_INTERFACE_BLOCK = ITEMS.register("inter_dimensional_interface", () -> new BlockItem(BlockInit.INTER_DIMENSIONAL_INTERFACE_BLOCK.get(), new Item.Properties().tab(TabInit.MAIN_TAB)));
}
