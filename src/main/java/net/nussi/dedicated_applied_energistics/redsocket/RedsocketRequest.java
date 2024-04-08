package net.nussi.dedicated_applied_energistics.redsocket;

import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.*;
import java.util.Base64;

public class RedsocketRequest implements Serializable {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String methodID;
    private final String requestID;

    private final String originID;
    private final Serializable data;

    public RedsocketRequest(String methodID, String requestID, String originID, Serializable data) {
        this.methodID = methodID;
        this.requestID = requestID;
        this.originID = originID;
        this.data = data;
    }

    public static RedsocketRequest fromDataString(String text) throws JsonSyntaxException, IOException {
        byte [] data = Base64.getDecoder().decode( text );
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(  data ) );
        Serializable o;
        try {
            o = (Serializable) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        ois.close();

        if(o instanceof RedsocketRequest request) {
            return request;
        }

        throw new JsonSyntaxException("Failed to parse");
    }

    public String toDataString() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( this );

        String data = Base64.getEncoder().encodeToString(baos.toByteArray());

        oos.close();
        baos.close();

        return data;
    }

    public RedsocketResponse buildResponse(Serializable responseData){
        return new RedsocketResponse(this.requestID, responseData);
    }

    public String getMethodID() {
        return methodID;
    }

    public String getRequestID() {
        return requestID;
    }

    public String getOriginID() {
        return originID;
    }

    public Serializable getData() {
        return data;
    }

    @Override
    public String toString() {
        return "RedsocketRequest{" +
                "methodID='" + methodID + '\'' +
                ", requestID='" + requestID + '\'' +
                ", data=" + data +
                '}';
    }
}
