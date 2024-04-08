package net.nussi.dedicated_applied_energistics.redsocket;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Redsocket implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final RedsocketServer server;

    private final Jedis jedis;
    private final JedisPool jedisPool;
    private final String cluster;
    private final String id;

    private final HashMap<String, RedsocketFunction<?, ?>> functions;
    public final List<RedsocketPair> pairs = new ArrayList<>();

    public Redsocket(String cluster, String redisURI) throws URISyntaxException {
        this.cluster = cluster;
        this.id = UUID.randomUUID().toString();

        this.jedisPool = new JedisPool(new URI(redisURI));
        this.functions = new HashMap<>();

        this.server = new RedsocketServer(jedisPool, this);

        this.jedis = jedisPool.getResource();
        this.jedis.hset("net/nussi/dedicated_applied_energistics/redsocket/clusters/" + cluster, this.id, this.getInfo().toDataString());

    }

    public RedsocketInfo getInfo() {
        return new RedsocketInfo(cluster, id);
    }

    public void registerFunction(String methodID, RedsocketFunction<?, ?> function) {
        this.jedis.hset("net/nussi/dedicated_applied_energistics/redsocket/functions", methodID, this.id);
        this.functions.put(methodID, function);
    }

    public String registerFunction(RedsocketFunction<?, ?> function) {
        String methodID = UUID.randomUUID().toString();
        this.jedis.hset("net/nussi/dedicated_applied_energistics/redsocket/functions", methodID, this.id);
        this.functions.put(methodID, function);
        return methodID;
    }

    public Serializable callLocal(String methodID, Serializable requestData) throws Exception {
        LOGGER.warn("Calling method on local redsocket instance is not recommended");
        RedsocketFunction<?, ?> function = functions.get(methodID);
        if (function == null) throw new IOException("Unknown method " + methodID);
        return function.callWithObjects(requestData);
    }

    public <N> N call(String methodID, Serializable requestData, Class<?> N, int timeout) throws Exception {
        return (N) call(methodID, requestData, timeout);
    }

    //Return the response data
    public Serializable call(String methodID, Serializable requestData, int timeout) throws Exception {
        String nodeID = this.jedis.hget("net/nussi/dedicated_applied_energistics/redsocket/functions", methodID);

        if(nodeID == null)  {
            throw new IOException("Unknown method id " + methodID);
        }

        if (nodeID.equals(this.id)) {
            return callLocal(methodID, requestData);
        }

        String nodeInfoString = this.jedis.hget("net/nussi/dedicated_applied_energistics/redsocket/clusters/" +cluster, nodeID);
        if(nodeInfoString == null) throw new IOException("Node is not in cluster " + cluster);
        RedsocketInfo nodeInfo = RedsocketInfo.fromDataString(nodeInfoString);
        if(!nodeInfo.getCluster().equals(this.cluster)) throw new IOException("Node is in cluster " + cluster + " but info is not correct");

        String requestID = UUID.randomUUID().toString();
        CompletableFuture<RedsocketResponse> responseFuture = new CompletableFuture<>();
        RedsocketRequest request = new RedsocketRequest(methodID, requestID, this.id, requestData);
        RedsocketPair pair = new RedsocketPair(request, responseFuture::complete);

        pairs.add(pair);
        jedis.publish(nodeID, request.toDataString());

        RedsocketResponse response = responseFuture.get(timeout, TimeUnit.MILLISECONDS);

        if (response == null) {
            LOGGER.error("Function call failed because no client was found that connects to " + nodeID);
            throw new IOException("Function call failed because no client was found that connects to " + nodeID);
        }

        return response.getData();
    }

    protected void call(RedsocketPair pair) throws Exception {
        RedsocketFunction<?, ?> function = functions.get(pair.getMethodId());
        Serializable responseObject = function.callWithObjects(pair.getRequest().getData());
        pair.fulfill(responseObject);
    }


    @Override
    public void close() throws Exception {
        this.server.close();

        this.jedis.hdel("net/nussi/dedicated_applied_energistics/redsocket/clusters/" + cluster, this.id);
        for (var methodID : this.functions.keySet()) {
            this.jedis.hdel("net/nussi/dedicated_applied_energistics/redsocket/functions", methodID);
        }

        this.jedisPool.close();
    }

    public void receiveResponse(RedsocketResponse response) {
        List<RedsocketPair> copy = pairs.stream().toList();
        for (var pair : copy) {
            if (!pair.getRequestID().equals(response.getRequestID())) continue;

            pairs.remove(pair);
            pair.fulfill(response.getData());
        }
    }
}
