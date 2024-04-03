package net.nussi.dedicated_applied_energistics.misc;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class LifecycleTickable extends Tickable {
    private boolean isRunning = false;
    private Thread thread = new Thread(this);
    private AtomicBoolean ticking = new AtomicBoolean(false);

    public boolean start() {
        if(isRunning) return true;

        if(!onStart()) return false;
        if(!this.startTicking()) return false;

        isRunning = true;
        return true;
    }

    public boolean stop() {
        if(!isRunning) return true;

        if(!this.stopTicking()) return false;
        if(!onStop()) return false;

        isRunning = false;
        return true;
    }

    protected abstract boolean onStart();
    protected abstract boolean onStop();

    public boolean isRunning() {
        return isRunning;
    }
}
