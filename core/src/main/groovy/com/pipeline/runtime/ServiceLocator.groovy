package com.pipeline.runtime

import com.pipeline.runtime.dsl.Steps
import com.pipeline.runtime.dsl.StepsExecutor
import com.pipeline.runtime.interfaces.IConfiguration
import com.pipeline.runtime.interfaces.ILogger

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Singleton
class ServiceLocator {
    private ConcurrentMap<Class<?>, Object> services = new ConcurrentHashMap<>();

    public static <T> T getService(Class<T> clazz) {
        synchronized (clazz) {
            return ServiceLocator.getInstance().services.get(clazz)
        }
    }

    public static <T>  void loadService(Class<T> clazz, T service) {
        synchronized (clazz) {
            ServiceLocator.getInstance().services.put(clazz, service);
        }
    }

    public static initialize() {
        IConfiguration configuration = new Configuration()
        ILogger loggerService = new Logger(configuration)
        loadService(IConfiguration.class, configuration)
        loadService(ILogger.class, loggerService)
        loadService(Steps.class, new StepsExecutor(configuration, loggerService))
    }
}