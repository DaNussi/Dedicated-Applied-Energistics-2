package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import com.mojang.logging.LogUtils;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import net.nussi.dedicated_applied_energistics.blockentities.InterDimensionalInterfaceBlockEntity;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.*;
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

    public InterDimensionalInterfaceCrafting(InterDimensionalInterfaceBlockEntity instance) {
        this.instance = instance;


        DedicatedAppliedEnergisticsController.addControllable(this);
        if(DedicatedAppliedEnergisticsController.IsRunning && !this.isRunning()) this.externalStart();
    }


    @Override
    protected void onStart() {
        LOGGER.info(uuid + " | Starting crafting provider!");
        this.networkPatterns = new NetworkPatternList(
                getJedis(),
                getJedis(),
                getJedis(),
                uuid
        );
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
                        networkPatterns.add(new NetworkPattern(pattern, this.uuid));
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
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
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

    public static class NetworkPatternList extends ArrayList<NetworkPattern> {
        private Jedis fetchJedis;
        private Jedis subJedis;
        private Jedis writeJedis;
        private String uuid;
        private Thread thread;
        private JedisPubSub pubSub;
        private NetworkPatternList instance;

        public NetworkPatternList(Jedis fetchJedis, Jedis subJedis, Jedis writeJedis, String uuid) {
            super();
            this.instance = this;
            this.fetchJedis = fetchJedis;
            this.subJedis = subJedis;
            this.writeJedis = writeJedis;
            this.uuid = uuid;
        }

        public void close() {
            if(!this.isEmpty()) {
                this.clear();
            }

            if(pubSub != null) {
                pubSub.unsubscribe(redisChannel());
                pubSub.unsubscribe();
                pubSub = null;
            }

            if(thread != null) {
                try {
                    thread.interrupt();
                    thread.join();
                    thread = null;
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        private void redisFetch() {
            Set<String> keys = fetchJedis.keys(redisChannel()+"/*.pattern");
            long maxCount = keys.size();
            int counter = 1;

            LOGGER.info("Fetching Patterns ...");
            for(String key : keys) {
                LOGGER.debug("Fetching Patterns [" + counter++ + "/" + maxCount + "]");

                try {
                    String data = fetchJedis.get(key);
                    NetworkPattern networkPattern = NetworkPattern.fromByteArray(data.getBytes());
                    if(networkPattern.origin.equals(uuid)) continue;
                    this.add(networkPattern);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }
            LOGGER.debug("Fetching Patterns finished");
        }

        private void redisSubscriber() {
            thread = new Thread(() -> {
                try {
                    pubSub = new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            try {
                                NetworkPattern networkPattern = NetworkPattern.fromByteArray(message.getBytes());
                                if(networkPattern.equals(uuid)) return;

                                if(networkPattern.exists) {
                                    instance.add(networkPattern);
                                } else {
                                    instance.remove(networkPattern);
                                }
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage());
                            }
                        }
                    };

                    subJedis.subscribe(pubSub, redisChannel());
                } catch (Exception e) {}
            });
            thread.start();
        }

        public String redisChannel() {
            return 0 + ".pattern";
        }

        @Override
        public boolean add(NetworkPattern networkPattern) {
            if(networkPattern.origin.equals(uuid)) {
                networkPattern.exists = true;
                writeJedis.publish(redisChannel(), new String(NetworkPattern.toByteArray(networkPattern)));
            }

            return super.add(networkPattern);
        }

        @Override
        public boolean remove(Object o) {
            if(o instanceof NetworkPattern networkPattern) {
                if(networkPattern.origin.equals(uuid)) {
                    networkPattern.exists = false;
                    writeJedis.publish(redisChannel(), new String(NetworkPattern.toByteArray(networkPattern)));
                }
            }

            return super.remove(o);
        }

        public boolean isRegistered(IPatternDetails pattern) {
            for(NetworkPattern networkPattern : this) {
                if(networkPattern.pattern == pattern) return true;
            }
            return false;
        }
    }

    public static class NetworkPattern implements Serializable {
        public IPatternDetails pattern;
        public String origin;
        public boolean exists;

        public NetworkPattern(IPatternDetails pattern, String origin) {
            this.pattern = pattern;
            this.origin = origin;
            this.exists = true;
        }

        /** Read the object from Base64 string. */
        public static NetworkPattern fromByteArray(byte [] data ) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(  data ) );
                NetworkPattern o  = (NetworkPattern) ois.readObject();
                ois.close();
                return o;
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        /** Write the object to a Base64 string. */
        public static byte[] toByteArray( NetworkPattern o ) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream( baos );
                oos.writeObject( o );
                oos.close();
                return baos.toByteArray();
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
                return new byte[0];
            }
        }
    }

    public static List<IPatternDetails> getPatterns(IGrid grid) {
        List<IPatternDetails> currentPatterns = new ArrayList<>();
        ICraftingService craftingService = grid.getCraftingService();
        craftingService.getCraftables(what -> true).forEach(aeKey -> currentPatterns.addAll(craftingService.getCraftingFor(aeKey)));
        return currentPatterns;
    }
}
