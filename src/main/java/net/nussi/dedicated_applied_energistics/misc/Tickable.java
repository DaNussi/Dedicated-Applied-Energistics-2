package net.nussi.dedicated_applied_energistics.misc;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Tickable implements Runnable {
    private static final Logger LOGGER = LogUtils.getLogger();

    private Thread thread = new Thread(this);
    private AtomicBoolean ticking = new AtomicBoolean(false);

    protected boolean startTicking() {
        if(ticking.get()) return true;

        try {
            ticking.set(true);
            thread.start();
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to start thread");
            e.printStackTrace();
            return false;
        }
    }

    public boolean stopTicking() {
        if(!ticking.get()) return true;

        try {
            ticking.set(false);
            thread.join(1000);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to stop thread");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void run() {
        while (ticking.get()) {
            try {
                onTick();
            } catch (Exception e) {
                LOGGER.error("Failed to tick");
                e.printStackTrace();
            }
        }
    }

    protected abstract void onTick() throws Exception;

    public boolean isTicking() {
        return ticking.get();
    }
}
