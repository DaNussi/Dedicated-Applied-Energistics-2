package net.nussi.dedicated_applied_energistics.websockets;

import com.mojang.logging.LogUtils;
import net.nussi.dedicated_applied_energistics.misc.LifecycleTickable;
import org.slf4j.Logger;

import java.io.*;
import java.net.Socket;

public class WebsocketClientHandler extends LifecycleTickable {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Socket socket;
    private WebsocketHost websocketHost;

    public WebsocketClientHandler(Socket socket, WebsocketHost websocketHost) {
        this.socket = socket;
        this.websocketHost = websocketHost;
    }

    InputStream is;
    OutputStream os;

    ObjectInput ois;
    ObjectOutput oos;

    @Override
    protected boolean onStart() {

        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();

            ois = new ObjectInputStream(is);
            oos = new ObjectOutputStream(os);
        } catch (Exception e) {
            LOGGER.error("Failed to open object output stream");
            return false;
        }

        return true;
    }

    @Override
    protected boolean onStop() {

        try {
            ois.close();
            is.close();

            oos.close();
            os.close();
        } catch (Exception e) {
            LOGGER.error("Failed to close object output stream");
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected void onTick() throws Exception {

        while (this.isTicking()) {

            if (ois.available() > 0) {
                Object object = ois.readObject();

                if (object instanceof WebsocketPaket paket) {
                    LOGGER.warn("Received a websocket paket");
                    websocketHost.handlePaket(paket);
                } else {
                    LOGGER.warn("Received a non websocket paket");
                }
            }

        }


    }
}
