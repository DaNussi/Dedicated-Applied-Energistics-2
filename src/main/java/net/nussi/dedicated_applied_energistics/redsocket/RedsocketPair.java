package net.nussi.dedicated_applied_energistics.redsocket;

import java.io.Serializable;

public class RedsocketPair {

    private final RedsocketRequest request;
    private final Callback callback;

    public RedsocketPair(RedsocketRequest request, Callback callback) {
        this.request = request;
        this.callback = callback;
    }

    public RedsocketRequest getRequest() {
        return request;
    }

    public void fulfill(Serializable responseData) {
        callback.callback(request.buildResponse(responseData));
    }

    public String getMethodId() {
        return request.getMethodID();
    }

    public String getRequestID() {return request.getRequestID();}

    public interface Callback {
        void callback(RedsocketResponse response);
    }

}
