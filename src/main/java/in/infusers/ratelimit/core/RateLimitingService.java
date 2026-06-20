package in.infusers.ratelimit.core;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import in.infusers.ratelimit.config.RateLimitStarterProperties;

/**
 * Redis-backed sliding-window rate limiter. One INCR per key per window;
 * TTL is set only on the first request in a window so the counter resets
 * cleanly without a separate cleanup job.
 */
@Service
public class RateLimitingService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingService.class);

    private final StringRedisTemplate redisTemplate;
    private final RateLimitStarterProperties properties;
    private final RateLimitEventListener eventListener;

    public RateLimitingService(StringRedisTemplate redisTemplate,
                                RateLimitStarterProperties properties,
                                RateLimitEventListener eventListener) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.eventListener = eventListener;
    }

    public boolean isAllowed(String key, int maxRequests, Duration timeWindow) {
        String redisKey = properties.getKeyPrefix() + key;
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        log.debug("isAllowed() key={} maxRequests={} timeWindow={} currentCount={}",
                key, maxRequests, timeWindow, currentCount);

        if (currentCount == null) {
            log.warn("isAllowed() -> increment returned null for key: {}", redisKey);
            return false;
        }

        if (currentCount == 1) {
            redisTemplate.expire(redisKey, timeWindow.getSeconds(), TimeUnit.SECONDS);
        }
        return currentCount <= maxRequests;
    }

    public void logRateLimitEvent(String type, String identifier, String reason) {
        logRateLimitEvent(type, identifier, reason, null, null);
    }

    public void logRateLimitEvent(String type, String identifier, String reason, String ipAddress, String userAgent) {
        eventListener.onRateLimitEvent(type, identifier, reason, ipAddress, userAgent);
    }
}
