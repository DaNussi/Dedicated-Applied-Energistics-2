package net.nussi.dedicated_applied_energistics.redsocket;

import java.io.Serializable;

public abstract class RedsocketFunction <T extends Serializable , N extends Serializable>{
    Serializable callWithObjects(Serializable obj) throws Exception {
        return (N) call((T) obj);
    }

    protected abstract N call(T obj) throws Exception;

    @Override
    public String toString() {
        return "RedsocketFunction{}";
    }
}
