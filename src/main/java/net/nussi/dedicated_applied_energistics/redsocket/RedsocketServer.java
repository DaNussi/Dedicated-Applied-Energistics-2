package net.nussi.dedicated_applied_energistics.redsocket;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.net.SocketException;

public class RedsocketServer implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Jedis jedisSub;
    private final Jedis jedisPub;
    private final Redsocket redsocket;
    private final Thread thread;
    private JedisPubSub pubSub;

    public RedsocketServer(JedisPool jedisPool, Redsocket redsocket) {
        this.jedisPub = jedisPool.getResource();
        this.jedisSub = jedisPool.getResource();

        this.redsocket = redsocket;
        this.thread = new Thread(this::run);
        this.thread.start();
    }

    public void run() {
        try {
            pubSub = new JedisPubSub() {
                @Override
                public void onSubscribe(String channel, int subscribedChannels) {
                    LOGGER.info("Subscribing to " + channel);
                }

                @Override
                public void onUnsubscribe(String channel, int subscribedChannels) {
                    LOGGER.info("Unsubscribe to " + channel);
                }

                @Override
                public void onMessage(String channel, String message) {
                    LOGGER.debug("Received message: " + message);

                    try {
                        RedsocketRequest request = RedsocketRequest.fromDataString(message);
                        if(request != null) {
                            LOGGER.debug("Received request: " + request);

                            RedsocketPair pair = new RedsocketPair(request, response -> {
                                LOGGER.debug("Sending response " + response + " to " + request.getOriginID());
                                LOGGER.debug("Received " + request + " --> Response " + response);
                                try {
                                    jedisPub.publish(request.getOriginID(), response.toDataString());
                                } catch (Exception e) {
                                    LOGGER.error("Failed to send response " + response + " to " + request.getOriginID());
                                    e.printStackTrace();
                                }
                            });
                            redsocket.call(pair);
                            return;
                        }
                    } catch (Exception e) {

                    }

                    try {
                        RedsocketResponse response = RedsocketResponse.fromDataString(message);
                        if(response != null) {
                            LOGGER.debug("Received response: " + response);

                            redsocket.receiveResponse(response);

                            return;
                        }
                    } catch (Exception e) {

                    }

                    LOGGER.error("Message couldn't be parsed : " + message);
                }
            };
            jedisSub.subscribe(pubSub, redsocket.getInfo().getId());
        } catch (Exception e) {
            if(e instanceof SocketException) return;
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        if(this.pubSub != null) this.pubSub.unsubscribe();
        this.thread.interrupt();
        this.jedisSub.close();
        this.jedisPub.close();
    }
}
