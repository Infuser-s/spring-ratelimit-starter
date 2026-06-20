package in.infusers.ratelimit.config;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "infusers.ratelimit.rules")
public class RateLimitProperties {

    private Map<String, RateLimitConfig> paths;

    public Map<String, RateLimitConfig> getPaths() {
        return paths;
    }

    public void setPaths(Map<String, RateLimitConfig> paths) {
        this.paths = paths.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> "/" + entry.getKey().replace('_', '/'),
                        Map.Entry::getValue
                ));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RateLimitProperties{paths={");
        if (paths != null) {
            paths.forEach((key, value) -> sb.append(key).append(": limit=").append(value.getLimit())
                    .append(", duration=").append(value.getDuration()).append(", "));
        }
        if (sb.length() > 24) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("}}");
        return sb.toString();
    }

    public static class RateLimitConfig {
        public static final int RATE_LIMIT_UNLIMITED = -1;
        private int limit;
        private Duration duration;

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

        public boolean isUnlimited() {
            return this.limit == RATE_LIMIT_UNLIMITED;
        }

        @Override
        public String toString() {
            return "RateLimitConfig [limit=" + limit + ", duration=" + duration + "]";
        }
    }
}
