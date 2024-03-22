package net.nussi.dedicated_applied_energistics.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics.MODID;

public class TabInit {
    public static DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final List<Supplier<? extends ItemLike>> MAIN_TAB_ITEMS = new ArrayList<>();


    public static RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("dae2_main_tab", () ->
            CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ItemInit.INTER_DIMENSIONAL_INTERFACE_BLOCK_ITEM.get()))
                    .title(Component.literal("Dedicated Applied Energistics 2"))
                    .displayItems((displayParams, output) ->
                            MAIN_TAB_ITEMS.forEach(itemLike -> output.accept(itemLike.get())))
                    .build());

    public static <T extends Item> RegistryObject<T> addToTab(RegistryObject<T> itemLike) {
        MAIN_TAB_ITEMS.add(itemLike);
        return itemLike;
    }
}
