package net.nussi.dedicated_applied_energistics.websockets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class WebsocketPaket implements Serializable {

    private String className;
    private String channel;
    private byte[] data;

    public WebsocketPaket(String channel, String className, byte[] data) {
        this.className = className;
        this.channel = channel;
        this.data = data;
    }

    public WebsocketPaket(String channel, Class<?> c, byte[] data) {
        this.channel = channel;
        this.className = c.getName();
        this.data = data;
    }

    public WebsocketPaket(String channel, Serializable obj) throws IOException {
        this.channel = channel;
        this.className = obj.getClass().getName();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        this.data = out.toByteArray();
        os.close();
        out.close();
    }

    public String getClassName() {
        return className;
    }

    public String getChannel() {
        return channel;
    }

    public byte[] getData() {
        return data;
    }
}
