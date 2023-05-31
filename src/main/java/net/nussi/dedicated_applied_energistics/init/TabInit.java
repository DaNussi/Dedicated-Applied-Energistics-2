package net.nussi.dedicated_applied_energistics.init;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class TabInit {

    public static final CreativeModeTab MAIN_TAB = new CreativeModeTab("dae2_main_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(BlockInit.INTER_DIMENSIONAL_INTERFACE_BLOCK.get());
        }
    };


}
