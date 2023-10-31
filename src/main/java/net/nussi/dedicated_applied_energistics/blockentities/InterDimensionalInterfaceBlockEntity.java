package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.settings.TickRates;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import net.nussi.dedicated_applied_energistics.init.ItemInit;
import net.nussi.dedicated_applied_energistics.providers.NetworkPattern;
import net.nussi.dedicated_applied_energistics.providers.NetworkPatternList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;


public class InterDimensionalInterfaceBlockEntity extends AENetworkBlockEntity implements ICraftingProvider, IGridTickable, ICraftingSimulationRequester, IActionSource {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected String hostUUID;
    protected NetworkPatternList networkPatternList;
    protected List<IPatternDetails> newVirtualPatterns = new ArrayList<>();
    protected List<IPatternDetails> oldVirtualPatterns = new ArrayList<>();

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get(), pos, blockState);

        this.getMainNode()
                .addService(ICraftingProvider.class, this)
                .addService(IGridTickable.class, this)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setVisualRepresentation(AEItemKey.of(ItemInit.INTER_DIMENSIONAL_INTERFACE_BLOCK.get()))
                .setIdlePowerUsage(5000);

        hostUUID = UUID.randomUUID().toString();
        networkPatternList = new NetworkPatternList(this.hostUUID);
    }

    private void doWork(int ticksSinceLastCall) {

        List<NetworkPattern> localNetworkPatterns = new ArrayList<>();
        this.getMainNode().getGrid().getMachines(PatternProviderBlockEntity.class)
                        .forEach(patternProviderBlockEntity -> patternProviderBlockEntity.getLogic().getAvailablePatterns()
                                .forEach(pattern -> {
                                    localNetworkPatterns.add(new NetworkPattern(this.hostUUID, pattern, patternProviderBlockEntity));
                                }));
        localNetworkPatterns.removeAll(networkPatternList.getRemoteNetworkPatterns());

        localNetworkPatterns.forEach(localNetworkPattern -> {
            if(!networkPatternList.instance().contains(localNetworkPattern)) {
                networkPatternList.add(localNetworkPattern);
                LOGGER.info(hostUUID + " | Added pattern to network! " + localNetworkPattern);
            }
        });

        networkPatternList.getLocalNetworkPatterns().forEach(localNetworkPattern -> {
            if(!localNetworkPatterns.contains(localNetworkPattern)) {
                networkPatternList.remove(localNetworkPattern);
                LOGGER.info(hostUUID + " | Removed pattern from network! " + localNetworkPattern);
            }
        });

        newVirtualPatterns = networkPatternList.getRemotePatterns();
        if(newVirtualPatterns != oldVirtualPatterns) {
            oldVirtualPatterns = newVirtualPatterns;
            try {
//                this.getMainNode().getGrid().getService(ICraftingService.class).refreshNodeCraftingProvider(this.getGridNode());
                ICraftingProvider.requestUpdate(this.getMainNode());
            } catch (Exception e) {
                LOGGER.error(hostUUID + " | Failed to update patterns!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return newVirtualPatterns;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        NetworkPattern networkPattern = networkPatternList.getByPattern(patternDetails);
        LOGGER.info(hostUUID + " | Pushing pattern! " + networkPattern);


        this.getMainNode().getGrid().getCraftingService().beginCraftingCalculation(this.getLevel(), this, patternDetails.getPrimaryOutput().what(),   patternDetails.getPrimaryOutput().amount(),CalculationStrategy.CRAFT_LESS );
        return networkPattern.pushPattern(inputHolder);
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Charger, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        doWork(ticksSinceLastCall);
        return TickRateModulation.FASTER;
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        data.putString("hostUUID", this.hostUUID);
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        // TODO ADD READING OF "hostUUID" data from nbt
    }


    @Nullable
    @Override
    public IActionSource getActionSource() {
        return this;
    }

    @Override
    public Optional<Player> player() {
        return Optional.empty();
    }

    @Override
    public Optional<IActionHost> machine() {
        return Optional.of(this);
    }

    @Override
    public <T> Optional<T> context(Class<T> key) {
        return Optional.empty();
    }
}
