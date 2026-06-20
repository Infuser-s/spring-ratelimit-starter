package in.infusers.library.ratelimit.alert;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class NoOpSecurityAlertListenerTest {

    @Test
    void onAlertDoesNotThrow() {
        NoOpSecurityAlertListener listener = new NoOpSecurityAlertListener();

        assertThatCode(() -> listener.onAlert("subject", "body")).doesNotThrowAnyException();
    }
}
