package in.infusers.ratelimit.core;

/**
 * Callback invoked whenever a rate limit or IP-security block fires.
 * Implement and register a bean of this type to forward events to your own
 * audit/observability system. {@link NoOpRateLimitEventListener} is the
 * default if no other implementation is registered.
 */
public interface RateLimitEventListener {

    void onRateLimitEvent(String type, String identifier, String reason, String ipAddress, String userAgent);
}
