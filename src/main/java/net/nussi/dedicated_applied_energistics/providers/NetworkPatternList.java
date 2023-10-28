package net.nussi.dedicated_applied_energistics.providers;

import appeng.api.crafting.IPatternDetails;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.Level;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.Set;

public class NetworkPatternList extends ArrayList<NetworkPattern> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Jedis fetchJedis;
    private Jedis subJedis;
    private Jedis writeJedis;
    private String uuid;
    private Thread thread;
    private JedisPubSub pubSub;
    private NetworkPatternList instance;
    private Level level;
    private JedisPool jedisPool;

    public NetworkPatternList(DedicatedAppliedEnergisticsController.Controllable controllable, String uuid, Level level) {
        super();
        LOGGER.debug("Creating NetworkPatternList");
        this.instance = this;
        this.jedisPool = new JedisPool(new JedisPoolConfig(), DedicatedAppliedEnergisticsController.CONFIG_VALUE_REDIS_URI.get());
        this.fetchJedis = jedisPool.getResource();
        this.subJedis = jedisPool.getResource();
        this.writeJedis = jedisPool.getResource();
        this.uuid = uuid;
        this.level = level;
        LOGGER.debug("Created NetworkPatternList");
    }

    public void close() {
        LOGGER.debug("Closing NetworkPatternList");
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

        if(jedisPool != null) {
            jedisPool.close();
            jedisPool = null;
        }

        LOGGER.debug("Closed NetworkPatternList");
    }

    void redisFetch() {
        Set<String> keys = fetchJedis.keys(redisChannel()+"/*.pattern");
        long maxCount = keys.size();
        int counter = 1;

        LOGGER.info("Fetching Patterns ...");
        for(String key : keys) {
            LOGGER.debug("Fetching Patterns [" + counter++ + "/" + maxCount + "]");

            try {
                String data = fetchJedis.get(key);
                NetworkPattern networkPattern = NetworkPattern.fromString(data, level, fetchJedis);
                if(networkPattern.origin.equals(uuid)) continue;
                this.add(networkPattern);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
        LOGGER.debug("Fetching Patterns finished");
    }

    void redisSubscriber() {
        thread = new Thread(() -> {
            try {
                pubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        try {
                            NetworkPattern networkPattern = NetworkPattern.fromString(message, level, fetchJedis);
                            if(networkPattern.origin.equals(uuid)) return;

                            if(networkPattern.exists) {
                                instance.add(networkPattern);
                                LOGGER.info("Added NetworkPattern to List " + networkPattern.origin  + " | " + uuid);
                            } else {
                                instance.remove(networkPattern);
                                LOGGER.info("Added NetworkPattern to List" + networkPattern.origin + " | " + uuid);
                            }
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage());
                            e.printStackTrace();
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
            LOGGER.debug("Adding NetworkPattern to Pub/Sub ...");
            writeJedis.publish(redisChannel(), NetworkPattern.toString(networkPattern));
            LOGGER.debug("Removing NetworkPattern to Pub/Sub Finished!");
        }

        return super.add(networkPattern);
    }

    @Override
    public boolean remove(Object o) {
        if(o instanceof NetworkPattern networkPattern) {
            if(networkPattern.origin.equals(uuid)) {
                networkPattern.exists = false;
                LOGGER.debug("Removing NetworkPattern to Pub/Sub ...");
                writeJedis.publish(redisChannel(), NetworkPattern.toString(networkPattern));
                LOGGER.debug("Removing NetworkPattern to Pub/Sub Finished!");
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
