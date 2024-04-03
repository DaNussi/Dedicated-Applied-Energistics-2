package net.nussi.dedicated_applied_energistics.websockets;

import net.nussi.dedicated_applied_energistics.misc.LifecycleTickable;

import java.io.*;
import java.net.Socket;

public class WebsocketClient extends LifecycleTickable {
    private Socket socket;

    public WebsocketClient(Socket socket) {
        this.socket = socket;
    }

    @Override
    protected boolean onStart() {
        return true;
    }

    @Override
    protected boolean onStop() {
        return true;
    }

    @Override
    protected void onTick() throws Exception {

    }

    public Socket getSocket() {
        return socket;
    }


    public void sendPaket(String channel, Object o) throws Exception {
        WebsocketConverter converter = WebsocketConverter.getConverter(o.getClass().getName());
        WebsocketPaket websocketPaket = new WebsocketPaket(channel, o.getClass().getName(), converter.toBytes(o));

        try {

            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            oos.writeObject(websocketPaket);

            oos.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
