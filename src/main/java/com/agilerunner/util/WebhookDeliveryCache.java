package com.agilerunner.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebhookDeliveryCache {

    private final Map<String, Long> cache = new ConcurrentHashMap<>();
    private final long ttlMillis = 1000 * 60 * 10; // 10분 유지

    public boolean isProcessed(String deliveryId) {
        Long timestamp = cache.get(deliveryId);
        if (timestamp == null) return false;

        if (System.currentTimeMillis() - timestamp > ttlMillis) {
            cache.remove(deliveryId);
            return false;
        }

        return true;
    }

    public void markProcessed(String deliveryId) {
        cache.put(deliveryId, System.currentTimeMillis());
    }
}
