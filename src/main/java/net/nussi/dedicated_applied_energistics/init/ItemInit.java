package net.nussi.dedicated_applied_energistics.init;

import appeng.api.storage.cells.StorageCell;
import appeng.core.definitions.ItemDefinition;
import appeng.items.storage.BasicStorageCell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import net.nussi.dedicated_applied_energistics.items.InterDimensionalStorageCell;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics.MODID;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<InterDimensionalStorageCell> INTER_DIMENSIONAL_STORAGE_CELL_ITEM = ITEMS.register("inter_dimensional_storage_cell", () -> new InterDimensionalStorageCell(new Item.Properties().tab(TabInit.MAIN_TAB).stacksTo(1)));


}
