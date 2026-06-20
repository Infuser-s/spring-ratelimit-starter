package in.infusers.library.ratelimit.alert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SecurityAlertServiceTest {

    @Mock
    private SecurityAlertListener listener;

    private SecurityAlertService service;

    @BeforeEach
    void setUp() {
        service = new SecurityAlertService(listener);
    }

    @Test
    void alertIpBreachNotifiesListener() {
        service.alertIpBreach("1.2.3.4", false, "High request rate");

        verify(listener).onAlert(contains("1.2.3.4"), contains("High request rate"));
    }

    @Test
    void secondAlertForSameKeyWithinCooldownIsSuppressed() {
        service.alertIpBreach("1.2.3.4", false, "High request rate");
        service.alertIpBreach("1.2.3.4", false, "High request rate");

        verify(listener, times(1)).onAlert(any(), any());
    }

    @Test
    void suspiciousAndNonSuspiciousBreachesForSameIpAreTrackedSeparately() {
        service.alertIpBreach("1.2.3.4", false, "High request rate");
        service.alertIpBreach("1.2.3.4", true, "Suspicious activity");

        verify(listener, times(2)).onAlert(any(), any());
    }

    @Test
    void apiRateLimitBreachNotifiesListener() {
        service.alertApiRateLimitBreach("/api/v1/orders");

        verify(listener).onAlert(contains("/api/v1/orders"), contains("API Path Rate Limit"));
    }

    @Test
    void differentEndpointsAreNotCooledDownTogether() {
        service.alertApiRateLimitBreach("/api/v1/orders");
        service.alertApiRateLimitBreach("/api/v1/users");

        verify(listener, times(2)).onAlert(any(), any());
    }
}
