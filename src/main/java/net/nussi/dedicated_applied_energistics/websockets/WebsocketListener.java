package net.nussi.dedicated_applied_energistics.websockets;

public abstract class WebsocketListener<T> {
    private String channel;
    private Class<?> classType;

    public WebsocketListener(String channel, Class<?> classType) {
        this.channel = channel;
        this.classType = classType;
    }

    public void reciveObject(Object object) {
        var obj = (T) object;
        receive(obj);
    }

    public abstract void receive(T object);

    public String getChannel() {
        return channel;
    }

    public String getClassName() {
        return classType.getName();
    }

}
