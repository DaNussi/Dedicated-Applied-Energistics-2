package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.Level;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import org.slf4j.Logger;

import java.util.*;

public class InterDimensionalInterfaceCrafting extends DedicatedAppliedEnergisticsController.Controllable implements ICraftingProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private String uuid = UUID.randomUUID().toString();
    private InterDimensionalInterfaceBlockEntity instance;

    private List<IPatternDetails> allPatterns = new ArrayList<>();
    private List<IPatternDetails> virtualPatterns = new ArrayList<>();
    private NetworkPatternList networkPatterns;
    private Thread thread;
    private boolean runningThread;
    private Level level;

    public InterDimensionalInterfaceCrafting(InterDimensionalInterfaceBlockEntity instance) {
        this.instance = instance;
        this.level = instance.getLevel();

        DedicatedAppliedEnergisticsController.addControllable(this);
        if(DedicatedAppliedEnergisticsController.IsRunning && !this.isRunning()) this.externalStart();
    }


    @Override
    protected void onStart() {
        LOGGER.info(uuid + " | Starting crafting provider!");
        this.networkPatterns = new NetworkPatternList(this, uuid, level);
        this.networkPatterns.redisFetch();
        this.networkPatterns.redisSubscriber();


        onStartPatternScanner();

        LOGGER.info(uuid + " | Started crafting provider!");
    }

    @Override
    protected void onStop() {
        LOGGER.info(uuid + " | Stopping crafting provider!");
        try {
            runningThread = false;
            if(thread != null) {
                thread.interrupt();
                thread.join();
                thread = null;
            }

            if(networkPatterns != null) networkPatterns.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        LOGGER.info(uuid + " | Stopped crafting provider!");
    }

    public void onStartPatternScanner() {
        thread = new Thread(() -> {
            while (runningThread) {
                if(instance == null) continue;
                if(instance.getMainNode() == null) continue;
                if(instance.getMainNode().getGrid() == null) continue;

                this.allPatterns = getPatterns(instance.getMainNode().getGrid());
                LOGGER.debug(uuid + " | All patterns: " + allPatterns.size());

                // Checken auf unregister Patterns
                LOGGER.debug(uuid + " | Checking for unregister patterns");
                this.allPatterns.forEach(pattern -> {
                    if(!networkPatterns.isRegistered(pattern)) {
                        NetworkPattern networkPattern = new NetworkPattern(pattern, this.uuid, true, this.level);
                        networkPattern.enableResponse = true;
                        networkPatterns.add(networkPattern);
                        LOGGER.info(uuid + " | Added new pattern to network!");
                    }
                });

                // Löschen von Network Pattern, wenn diese nicht mehr vorhanden sind
                LOGGER.debug(uuid + " | Deleting non existed patterns from network!");
                List<NetworkPattern> pendingDeletion = new ArrayList<>();
                for(NetworkPattern networkPattern : networkPatterns) {
                    if(!networkPattern.origin.equals(this.uuid)) {
                        LOGGER.debug(uuid + " | Skipping patter because it's not from this IDI");
                        continue; // Überspringe wenn nicht von diesem IDI
                    }
                    if(allPatterns.contains(networkPattern.pattern)) {
                        LOGGER.debug(uuid + " | Skipping patter because it still exists");
                        continue; // Überspringe wenn Pattern Local vorhanden
                    }
                    pendingDeletion.add(networkPattern); // Löschen des Network Patterns
                    LOGGER.debug(uuid + " | Pending deletion of pattern from network!");
                }
                pendingDeletion.forEach(pattern -> {
                    networkPatterns.remove(pattern);
                    LOGGER.info(uuid + " | Deleted pattern from network!");
                });

                // Updaten der Virtuellen Patterns
                LOGGER.debug(uuid + " | Updating virtual patterns!");
                virtualPatterns = new ArrayList<>();
                for(NetworkPattern networkPattern : networkPatterns) {
                    if(networkPattern.origin.equals(this.uuid)) {
                        LOGGER.debug(uuid + " | Skipping patter because it's from this IDI");
                        continue;
                    }
                    virtualPatterns.add(networkPattern.pattern);
                    LOGGER.debug(uuid + " | Added virtual pattern!");
                }

                LOGGER.debug(uuid + " | NetworkPatterns[" + networkPatterns.size() + "]: " + Arrays.toString(networkPatterns.toArray()));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
        });

        runningThread = true;
        thread.start();
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return virtualPatterns;
    }

    @Override
    public int getPatternPriority() {
        return ICraftingProvider.super.getPatternPriority();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {

        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public Set<AEKey> getEmitableItems() {
        return ICraftingProvider.super.getEmitableItems();
    }


    public static List<IPatternDetails> getPatterns(IGrid grid) {
        List<IPatternDetails> currentPatterns = new ArrayList<>();
        ICraftingService craftingService = grid.getCraftingService();
        craftingService.getCraftables(what -> true).forEach(aeKey -> currentPatterns.addAll(craftingService.getCraftingFor(aeKey)));
        return currentPatterns;
    }
}
