package in.infusers.library.ratelimit.config;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitStarterPropertiesTest {

    @Test
    void defaultKeyPrefixIsRatelimit() {
        RateLimitStarterProperties properties = new RateLimitStarterProperties();

        assertThat(properties.getKeyPrefix()).isEqualTo("ratelimit:");
        assertThat(properties.getExcludedPaths()).isEmpty();
    }

    @Test
    void settersOverrideDefaults() {
        RateLimitStarterProperties properties = new RateLimitStarterProperties();
        properties.setKeyPrefix("myapp:");
        properties.setExcludedPaths(List.of("/version"));

        assertThat(properties.getKeyPrefix()).isEqualTo("myapp:");
        assertThat(properties.getExcludedPaths()).containsExactly("/version");
    }
}
