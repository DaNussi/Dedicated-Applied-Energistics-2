package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.iface.PatternProviderLogic;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import net.nussi.dedicated_applied_energistics.init.ItemInit;
import net.nussi.dedicated_applied_energistics.providers.InterDimensionalInterfacePatternProvider;
import org.slf4j.Logger;


public class InterDimensionalInterfaceBlockEntity extends PatternProviderBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get(), pos, blockState);
    }

    @Override
    protected PatternProviderLogic createLogic() {
        return new InterDimensionalInterfacePatternProvider(this.getMainNode(), this);
    }

    @Override
    public void onReady() {
        super.onReady();

//        if(this.getMainNode().getGrid().getMachines(InterDimensionalInterfaceBlockEntity.class).size() >= 1) {
//            this.getBlockState().getBlock().des
//        }
//
//        this.getMainNode().getGrid().getMachines(PatternProviderBlockEntity.class)
//                .forEach(patternProviderBlockEntity -> {
//                    patternProviderBlockEntity.get
//                });


    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(ItemInit.INTER_DIMENSIONAL_INTERFACE_BLOCK.get());
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(ItemInit.INTER_DIMENSIONAL_INTERFACE_BLOCK.get());
    }
}
