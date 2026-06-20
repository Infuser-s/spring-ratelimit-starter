package in.infusers.library.ratelimit.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class NoOpRateLimitEventListenerTest {

    @Test
    void onRateLimitEventDoesNotThrow() {
        NoOpRateLimitEventListener listener = new NoOpRateLimitEventListener();

        assertThatCode(() -> listener.onRateLimitEvent("API_PATH", "/foo", "reason", "1.2.3.4", "curl"))
                .doesNotThrowAnyException();
    }
}
