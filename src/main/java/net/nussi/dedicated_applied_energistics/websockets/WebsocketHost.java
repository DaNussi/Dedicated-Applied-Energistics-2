package net.nussi.dedicated_applied_energistics.websockets;

import com.mojang.logging.LogUtils;
import net.nussi.dedicated_applied_energistics.misc.Lifecycle;
import net.nussi.dedicated_applied_energistics.misc.LifecycleTickable;
import net.nussi.dedicated_applied_energistics.misc.Tickable;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController.CONFIG_VALUE_HOST_IP;
import static net.nussi.dedicated_applied_energistics.DedicatedAppliedEnergisticsController.CONFIG_VALUE_HOST_PORT;

public class WebsocketHost extends LifecycleTickable {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ServerSocket server;
    private List<WebsocketClientHandler> clients = new ArrayList<>();
    private List<WebsocketListener<?>> listeners = new ArrayList<>();

    @Override
    protected boolean onStart() {
        try {
            int port = CONFIG_VALUE_HOST_PORT.get();
            String ip = CONFIG_VALUE_HOST_IP.get();
            LOGGER.info("Starting websocket host on with " + ip + ":" + port);
            server = new ServerSocket(port, 10, InetAddress.getByName(ip));
        } catch (IOException e) {
            LOGGER.error("Failed to start websocket host socket");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    protected boolean onStop() {
        try {
            if (server != null) server.close();
        } catch (IOException e) {
            LOGGER.error("Failed to stop websocket host socket");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    protected void onTick() throws Exception {
        if (server == null) {
            LOGGER.warn("Skipping tick because server is null");
            return;
        }

        try {
            Socket socket = server.accept();
            LOGGER.info("Accepted socket from " + socket.getRemoteSocketAddress().toString());

            var handler = new WebsocketClientHandler(socket, this);
            handler.start();
            clients.add(handler);
        } catch (IOException e) {
            LOGGER.error("Failed to accept socket");
            e.printStackTrace();
        }
    }

    public void registerListener(WebsocketListener<?> listener) {
        listeners.add(listener);
    }

    public void handlePaket(WebsocketPaket paket) {
        for (int i = 0; i < listeners.size(); i++) {
            var listener = listeners.get(i);

            if (!paket.getChannel().equals(listener.getChannel())) continue;
            if (!paket.getClassName().equals(listener.getClassName())) continue;

            WebsocketConverter<?> converter = WebsocketConverter.getConverter(paket.getClassName());
            if(converter == null) {
                LOGGER.error("No converter found for " + paket.getClassName());
                continue;
            }

            Object obj;
            try {
                obj = converter.fromBytes(paket.getData());
            } catch (Exception e) {
                LOGGER.error("Failed to convert object");
                continue;
            }

            listener.reciveObject(obj);
        }

    }


}
