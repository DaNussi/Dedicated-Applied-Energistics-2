package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.me.service.CraftingService;
import com.mojang.logging.LogUtils;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.*;

public class InterDimensionalInterfaceCrafting extends DedicatedAppliedEnergisticsController.Controllable implements ICraftingProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private String uuid = UUID.randomUUID().toString();
    private InterDimensionalInterfaceBlockEntity instance;
    private Jedis jedis;

    private List<IPatternDetails> allPatterns = new ArrayList<>();
    private List<IPatternDetails> virtualPatterns = new ArrayList<>();
    private NetworkPatternList networkPatterns = new NetworkPatternList();
    private Thread thread;
    private boolean runningThread;

    public InterDimensionalInterfaceCrafting(InterDimensionalInterfaceBlockEntity instance) {
        this.instance = instance;


        DedicatedAppliedEnergisticsController.addControllable(this);
        if(DedicatedAppliedEnergisticsController.IsRunning && !this.isRunning()) this.externalStart();
    }


    @Override
    protected void onStart() {
        LOGGER.info(uuid + " | Starting crafting provider!");
        this.jedis = getJedis();

        thread = new Thread(() -> {
            while (runningThread) {
                if(instance == null) continue;
                if(instance.getMainNode() == null) continue;
                if(instance.getMainNode().getGrid() == null) continue;

                this.allPatterns = PatterScanner.getPatterns(instance.getMainNode().getGrid());
                LOGGER.debug(uuid + " | All patterns: " + allPatterns.size());

                // Checken auf unregister Patterns
                LOGGER.debug(uuid + " | Checking for unregister patterns");
                this.allPatterns.forEach(pattern -> {
                    if(!networkPatterns.isRegistered(pattern)) {
                        networkPatterns.add(new NetworkPattern(pattern, this.uuid));
                        LOGGER.info(uuid + " | Added new pattern to network!");
                    }
                });

                // Löschen von Network Pattern, wenn diese nicht mehr vorhanden sind
                LOGGER.debug(uuid + " | Deleting non existed patterns from network!");
                NetworkPatternList pendingDeletion = new NetworkPatternList();
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
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        runningThread = true;
        thread.start();

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
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
        LOGGER.info(uuid + " | Stopped crafting provider!");
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

    public static class NetworkPatternList extends ArrayList<NetworkPattern> {
        public List<IPatternDetails> toPatternList() {
            List<IPatternDetails> patterns = new ArrayList<>();
            this.forEach(networkPattern -> patterns.add(networkPattern.pattern));
            return patterns;
        }

        public boolean isRegistered(IPatternDetails pattern) {
            for(NetworkPattern networkPattern : this) {
                if(networkPattern.pattern == pattern) return true;
            }
            return false;
        }
    }

    public static class NetworkPattern {
        IPatternDetails pattern;
        String origin;

        public NetworkPattern(IPatternDetails pattern, String origin) {
            this.pattern = pattern;
            this.origin = origin;
        }
    }

    public static class PatterScanner implements Runnable {
        private Listener listener;
        private Thread thread;
        private IGrid grid;
        private boolean running;

        public PatterScanner(IGrid grid, Listener listener) {
            this.listener = listener;
            this.grid = grid;
            this.running = true;
            this.thread = new Thread(this);
            this.thread.start();
        }

        public void close() throws InterruptedException {
            running = false;
            thread.join();
        }

        @Override
        public void run() {
            while (running) {
                List<IPatternDetails> currentPatterns = getPatterns(grid);

            }
        }

        public interface Listener {
            void onAddPatter();
            void onRemovePatter();
        }

        public static List<IPatternDetails> getPatterns(IGrid grid) {
            List<IPatternDetails> currentPatterns = new ArrayList<>();
            ICraftingService craftingService = grid.getCraftingService();
            craftingService.getCraftables(what -> true).forEach(aeKey -> currentPatterns.addAll(craftingService.getCraftingFor(aeKey)));
            return currentPatterns;
        }
    }
}
