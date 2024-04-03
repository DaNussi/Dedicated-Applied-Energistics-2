package net.nussi.dedicated_applied_energistics.misc;

public abstract class Lifecycle {
    private boolean isRunning = false;

    public boolean start() {
        if(isRunning) return true;
        if(onStart()) {
            isRunning = true;
            return true;
        } else {
            return false;
        }
    }

    public boolean stop() {
        if(!isRunning) return true;
        if(onStop()) {
            isRunning = false;
            return true;
        } else {
            return false;
        }
    }

    protected abstract boolean onStart();
    protected abstract boolean onStop();

    public boolean isRunning() {
        return isRunning;
    }
}
