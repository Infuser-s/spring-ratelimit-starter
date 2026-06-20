package in.infusers.library.ratelimit.config;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import in.infusers.library.ratelimit.config.RateLimitProperties.RateLimitConfig;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitPropertiesTest {

    @Test
    void setPathsConvertsUnderscoresToSlashesWithLeadingSlash() {
        RateLimitProperties properties = new RateLimitProperties();
        Map<String, RateLimitConfig> input = new LinkedHashMap<>();
        RateLimitConfig config = new RateLimitConfig();
        config.setLimit(10);
        config.setDuration(Duration.ofMinutes(1));
        input.put("users_login", config);

        properties.setPaths(input);

        assertThat(properties.getPaths()).containsOnlyKeys("/users/login");
        assertThat(properties.getPaths().get("/users/login").getLimit()).isEqualTo(10);
    }

    @Test
    void pathWithoutUnderscoreGetsOnlyLeadingSlash() {
        RateLimitProperties properties = new RateLimitProperties();
        Map<String, RateLimitConfig> input = new LinkedHashMap<>();
        input.put("actuator", new RateLimitConfig());

        properties.setPaths(input);

        assertThat(properties.getPaths()).containsOnlyKeys("/actuator");
    }

    @Test
    void negativeOneLimitIsUnlimited() {
        RateLimitConfig config = new RateLimitConfig();
        config.setLimit(-1);

        assertThat(config.isUnlimited()).isTrue();
    }

    @Test
    void positiveLimitIsNotUnlimited() {
        RateLimitConfig config = new RateLimitConfig();
        config.setLimit(10);

        assertThat(config.isUnlimited()).isFalse();
    }
}
