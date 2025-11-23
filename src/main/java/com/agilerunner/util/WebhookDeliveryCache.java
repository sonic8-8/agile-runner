package com.agilerunner.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebhookDeliveryCache {
    private static final long TTL_MILLIS = Duration.ofMinutes(10).toMillis();
    private final Map<String, Long> cache = new ConcurrentHashMap<>();

    public boolean isDuplicate(String deliveryId) {
        Long timestamp = findTimestamp(deliveryId);

        if (isNotCached(timestamp)) {
            return false;
        }
        if (isExpired(timestamp)) {
            evict(deliveryId);
            return false;
        }

        return true;
    }

    public void record(String deliveryId) {
        cache.put(deliveryId, now());
    }

    private Long findTimestamp(String deliveryId) {
        return cache.get(deliveryId);
    }

    private boolean isNotCached(Long timestamp) {
        return timestamp == null;
    }

    private boolean isExpired(Long timestamp) {
        return now() - timestamp > TTL_MILLIS;
    }

    private void evict(String deliveryId) {
        cache.remove(deliveryId);
    }

    private long now() {
        return System.currentTimeMillis();
    }
}
