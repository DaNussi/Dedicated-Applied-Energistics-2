package net.nussi.dedicated_applied_energistics.websockets;

import net.nussi.dedicated_applied_energistics.misc.LifecycleTickable;

import java.net.Socket;
import java.util.HashMap;

public class WebsocketClientManager extends LifecycleTickable {
    HashMap<String, WebsocketClient> clients = new HashMap<>();

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

    public WebsocketClient getClient(String ip, int port) throws Exception {
        String index = getClientIndex(ip, port);

        WebsocketClient client = clients.get(index);
        if(client == null) {
            Socket socket = new Socket(ip, port);
            client = new WebsocketClient(socket);
            client.start();
        }

        if(client.getSocket().isClosed()) {
            client.stop();
            Socket socket = new Socket(ip, port);
            client = new WebsocketClient(socket);
            client.start();
        }

        clients.put(index, client);

        return client;
    }

    private String getClientIndex(String ip, int port) {
        return ip + ":" + port;
    }

}
