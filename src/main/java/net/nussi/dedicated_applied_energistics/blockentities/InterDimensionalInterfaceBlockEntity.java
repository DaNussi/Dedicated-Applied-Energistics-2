package net.nussi.dedicated_applied_energistics.blockentities;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.InterfaceLogicHost;
import appeng.me.service.CraftingService;
import appeng.me.service.helpers.NetworkCraftingProviders;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.nussi.dedicated_applied_energistics.init.BlockEntityTypeInit;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class InterDimensionalInterfaceBlockEntity extends AENetworkBlockEntity implements ICraftingProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Thread thread;
    public static ConcurrentHashMap<IPatternDetails, String> allPatterns = new ConcurrentHashMap<>();
    private static int UUIDCounter = 0;
//    private String localUUID = UUID.randomUUID().toString();
    private String localUUID = "uuid-"+(UUIDCounter++);

    private List<IPatternDetails> localPatterns = new ArrayList<>();
    private List<IPatternDetails> oldLocalPatterns = new ArrayList<>();
    private List<IPatternDetails> virtualPatterns = new ArrayList<>();
    private List<IPatternDetails> oldVirtualPatterns = new ArrayList<>();

    public InterDimensionalInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.INTER_DIMENSIONAL_INTERFACE_ENTITY_TYPE.get(), pos, state);


        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(ICraftingProvider.class, this);

        this.thread = new Thread(this::run);
        this.thread.start();
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return virtualPatterns;
    }

    @Override
    public int getPatternPriority() {
        return 0;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        return true;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public Set<AEKey> getEmitableItems() {
        return Collections.EMPTY_SET;
    }


    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                LOGGER.debug(localUUID + " | Checking Patterns");
                if(this.getMainNode() == null) continue;
                if(this.getMainNode().getGrid() == null) continue;
                if(this.getMainNode().getGrid().getCraftingService() == null) continue;

                localPatterns.clear();
                this.getMainNode().getGrid().getCraftingService()
                        .getCraftables(what -> true)
                        .forEach(aeKey -> localPatterns.addAll(this.getMainNode().getGrid().getCraftingService().getCraftingFor(aeKey)));
                localPatterns.removeAll(virtualPatterns);
                localPatterns.forEach(pattern -> LOGGER.debug(localUUID + " | Added local Patterns: " + pattern.getPrimaryOutput().what().getDisplayName()));

                localPatterns.forEach(localPattern -> {
                    if(!allPatterns.containsKey(localPattern)) { // Wenn local pattern nicht in remote
                        allPatterns.put(localPattern, localUUID);
                        LOGGER.info(localUUID + " | Added pattern from local! " + localPattern.getPrimaryOutput().what().getDisplayName());
                    }
                });


                virtualPatterns.clear();
                allPatterns.forEach((pattern, remoteUUID) -> {
                    if(remoteUUID.equals(localUUID)) { // Wenn Pattern von Local ist
                        if(!localPatterns.contains(pattern)) { // Wenn Pattern nicht mehr vorhanden ist
                            allPatterns.remove(pattern); // Lösche das Pattern von Remote
                            LOGGER.debug(localUUID + " | Removed pattern from local! " + pattern.getPrimaryOutput().what().getDisplayName());
                        }
                    } else {
                        virtualPatterns.add(pattern);
                        LOGGER.debug(localUUID + " | Added virtual Patterns: " + pattern.getPrimaryOutput().what().getDisplayName());
                    }
                });

                if (oldVirtualPatterns != virtualPatterns) {
                    oldVirtualPatterns = virtualPatterns;
                    this.getMainNode().getGrid().getService(ICraftingService.class).refreshNodeCraftingProvider(this.getGridNode());
                    LOGGER.info(localUUID + " | Updated virtual Patterns: " + virtualPatterns);
                }


                if (oldLocalPatterns != localPatterns) {
                    oldLocalPatterns = localPatterns;
                    LOGGER.info(localUUID + " | Updated local Patterns: " + localPatterns);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
