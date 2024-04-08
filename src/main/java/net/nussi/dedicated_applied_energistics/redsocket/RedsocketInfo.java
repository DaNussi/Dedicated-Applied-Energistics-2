package net.nussi.dedicated_applied_energistics.redsocket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;

public class RedsocketInfo implements Serializable {
    private static final Gson gson = new Gson();

    private String cluster;
    private String id;

    public RedsocketInfo(String cluster, String id) {
        this.cluster = cluster;
        this.id = id;
    }

    public static RedsocketInfo fromDataString(String data) throws JsonSyntaxException {
        return gson.fromJson(data, RedsocketInfo.class);
    }

    public String toDataString() {
        return gson.toJson(this);
    }


    public String getCluster() {
        return cluster;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "RedsocketInfo{" +
                "cluster='" + cluster + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
