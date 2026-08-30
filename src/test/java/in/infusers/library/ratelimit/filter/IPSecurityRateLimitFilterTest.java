package in.infusers.library.ratelimit.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import in.infusers.library.ratelimit.alert.SecurityAlertService;
import in.infusers.library.ratelimit.config.IpSecurityProperties;
import in.infusers.library.ratelimit.core.RateLimitingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IPSecurityRateLimitFilterTest {

    @Mock
    private RateLimitingService rateLimitingService;
    @Mock
    private SecurityAlertService securityAlertService;

    private IpSecurityProperties properties;
    private IPSecurityRateLimitFilter filter;

    private void init() {
        properties = new IpSecurityProperties();
        filter = new IPSecurityRateLimitFilter(rateLimitingService, properties, securityAlertService);
    }

    @Test
    void whitelistedIpBypassesRateLimit() throws Exception {
        init();
        properties.getWhitelistedIps().add("127.0.0.1");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verify(rateLimitingService, never()).isAllowed(anyString(), anyInt(), any());
    }

    @Test
    void whitelistedCidrBypassesRateLimit() throws Exception {
        init();
        properties.getWhitelistedCidrs().add("10.0.0.0/8");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.1.2.3");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verify(rateLimitingService, never()).isAllowed(anyString(), anyInt(), any());
    }

    @Test
    void spoofedForwardedHeaderCannotFakeWhitelistedIpWithoutTrustedProxyConfigured() throws Exception {
        // Regression test for the fix: whitelisting 127.0.0.1 must not be bypassable by an
        // arbitrary caller simply sending X-Forwarded-For: 127.0.0.1 (the exact example value
        // this project's own README shows for whitelisted-ips).
        init();
        properties.getWhitelistedIps().add("127.0.0.1");
        when(rateLimitingService.isAllowed(anyString(), anyInt(), any())).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.9");
        request.addHeader("X-Forwarded-For", "127.0.0.1");
        request.addHeader("User-Agent", "Mozilla/5.0");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verify(rateLimitingService).isAllowed(eq("ip_security:203.0.113.9"), anyInt(), any());
    }

    @Test
    void honorsForwardedHeaderOnlyWhenPeerIsConfiguredTrustedProxy() throws Exception {
        init();
        properties.getTrustedProxies().add("10.0.0.1");
        when(rateLimitingService.isAllowed(anyString(), anyInt(), any())).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "1.2.3.4");
        request.addHeader("User-Agent", "Mozilla/5.0");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verify(rateLimitingService).isAllowed(eq("ip_security:1.2.3.4"), anyInt(), any());
    }

    @Test
    void exemptUserAgentBypassesRateLimit() throws Exception {
        init();
        properties.getExemptUserAgents().add("UptimeRobot");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("9.9.9.9");
        request.addHeader("User-Agent", "UptimeRobot/2.0");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verify(rateLimitingService, never()).isAllowed(anyString(), anyInt(), any());
    }

    @Test
    void validInternalApiKeyBypassesRateLimit() throws Exception {
        init();
        properties.getInternalTools().getValidApiKeys().add("secret-key");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("9.9.9.9");
        request.addHeader("X-Internal-API-Key", "secret-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verify(rateLimitingService, never()).isAllowed(anyString(), anyInt(), any());
    }

    @Test
    void blocksAndAlertsWhenOverLimit() throws Exception {
        init();
        when(rateLimitingService.isAllowed(anyString(), anyInt(), any())).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("9.9.9.9");
        request.addHeader("User-Agent", "Mozilla/5.0");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
        verify(rateLimitingService).logRateLimitEvent(eq("IP_SECURITY"), eq("9.9.9.9"), anyString(), eq("9.9.9.9"), anyString());
        verify(securityAlertService).alertIpBreach(eq("9.9.9.9"), eq(false), anyString());
    }

    @Test
    void missingUserAgentIsTreatedAsSuspiciousAndUsesSuspiciousThreshold() throws Exception {
        init();
        properties.setSuspiciousThreshold(3);
        when(rateLimitingService.isAllowed(anyString(), eq(3), any())).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("9.9.9.9");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verify(rateLimitingService).isAllowed(eq("ip_security:9.9.9.9:suspicious"), eq(3), any());
    }

    @Test
    void allowsRequestUnderLimitAndContinuesChain() throws Exception {
        init();
        when(rateLimitingService.isAllowed(anyString(), anyInt(), any())).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("9.9.9.9");
        request.addHeader("User-Agent", "Mozilla/5.0");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
