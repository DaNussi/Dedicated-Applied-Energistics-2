package net.nussi.dedicated_applied_energistics.websockets;

import java.util.HashMap;

public abstract class WebsocketConverter<T> {
    private static final HashMap<String, WebsocketConverter<?>> converters = new HashMap<>();

    private final Class<T> clazz;

    public WebsocketConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    public abstract T fromBytes(byte[] bytes) throws Exception;
    public abstract byte[] toBytes(T obj) throws Exception;

    public String getClassName() {
        return clazz.getName();
    }


    public static WebsocketConverter getConverter(String className) {
        return converters.get(className);
    }

    public static void registerConverter(WebsocketConverter<?> converter) {
        converters.put(converter.getClassName(), converter);
    }
}
