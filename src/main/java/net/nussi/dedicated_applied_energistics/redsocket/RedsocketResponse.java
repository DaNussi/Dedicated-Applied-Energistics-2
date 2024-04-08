package net.nussi.dedicated_applied_energistics.redsocket;

import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.*;
import java.util.Base64;

public class RedsocketResponse implements Serializable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String requestID;
    private final Serializable data;

    public RedsocketResponse(String requestID, Serializable data) {
        this.requestID = requestID;
        this.data = data;
    }


    public static RedsocketResponse fromDataString(String text) throws JsonSyntaxException, IOException {
        byte [] data = Base64.getDecoder().decode( text );
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(  data ) );
        Serializable o;
        try {
            o = (Serializable) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        ois.close();

        if(o instanceof RedsocketResponse request) {
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

    public String getRequestID() {
        return requestID;
    }

    public Serializable getData() {
        return data;
    }

    @Override
    public String toString() {
        return "RedsocketResponse{" +
                "requestID='" + requestID + '\'' +
                ", data=" + data +
                '}';
    }
}
