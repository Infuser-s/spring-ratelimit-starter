package in.infusers.ratelimit.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Top-level configuration for the starter, independent of the per-path and
 * IP-security property groups. Controls the Redis key prefix used by
 * {@link in.infusers.ratelimit.core.RateLimitingService} and the path
 * exclusions used by {@link in.infusers.ratelimit.core.EndpointNormalizer}.
 */
@ConfigurationProperties(prefix = "infusers.ratelimit")
public class RateLimitStarterProperties {

    /** Prefix applied to every Redis key this starter writes. */
    private String keyPrefix = "ratelimit:";

    /**
     * Path prefixes that should bypass dynamic-segment normalization entirely
     * (returned unchanged). Use for endpoints whose path contains a fixed
     * segment that looks numeric/dynamic but isn't, e.g. "/version".
     */
    private List<String> excludedPaths = new ArrayList<>();

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }
}
