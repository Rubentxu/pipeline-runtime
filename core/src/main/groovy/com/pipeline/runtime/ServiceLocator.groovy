package com.pipeline.runtime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Singleton
class ServiceLocator {
    private ConcurrentMap<Class<?>, Object> services = new ConcurrentHashMap<>();

    public  <T> T getService(Class<T> clazz) {
        synchronized (clazz) {
            return ServiceLocator.getInstance().services.get(clazz)
        }
    }

    public <T>  void loadService(Class<T> clazz, T service) {
        synchronized (clazz) {
            services.put(clazz, service);
        }
    }
}