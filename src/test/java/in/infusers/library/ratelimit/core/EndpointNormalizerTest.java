package in.infusers.library.ratelimit.core;

import java.util.List;

import org.junit.jupiter.api.Test;

import in.infusers.library.ratelimit.config.RateLimitStarterProperties;

import static org.assertj.core.api.Assertions.assertThat;

class EndpointNormalizerTest {

    private EndpointNormalizer normalizer(RateLimitStarterProperties properties) {
        return new EndpointNormalizer(properties);
    }

    private EndpointNormalizer normalizer() {
        return normalizer(new RateLimitStarterProperties());
    }

    @Test
    void collapsesNumericSegment() {
        assertThat(normalizer().normalize("/users/42")).isEqualTo("/users/{variable}");
    }

    @Test
    void groupsDifferentNumericIdsUnderSameBucket() {
        EndpointNormalizer normalizer = normalizer();

        assertThat(normalizer.normalize("/users/42")).isEqualTo(normalizer.normalize("/users/43"));
    }

    @Test
    void collapsesEmailSegment() {
        assertThat(normalizer().normalize("/users/jane@example.com")).isEqualTo("/users/{variable}");
    }

    @Test
    void leavesNonDynamicSegmentsUnchanged() {
        assertThat(normalizer().normalize("/swagger-ui/index")).isEqualTo("/swagger-ui/index");
    }

    @Test
    void returnsExcludedPathUnchangedEvenWithNumericSegment() {
        RateLimitStarterProperties properties = new RateLimitStarterProperties();
        properties.setExcludedPaths(List.of("/version"));

        assertThat(normalizer(properties).normalize("/version/42")).isEqualTo("/version/42");
    }

    @Test
    void collapsesMultipleDynamicSegmentsInOnePath() {
        assertThat(normalizer().normalize("/users/42/orders/99")).isEqualTo("/users/{variable}/orders/{variable}");
    }
}
