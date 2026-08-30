package in.infusers.library.ratelimit.filter;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import in.infusers.library.ratelimit.alert.SecurityAlertService;
import in.infusers.library.ratelimit.config.IpSecurityProperties;
import in.infusers.library.ratelimit.config.RateLimitProperties;
import in.infusers.library.ratelimit.config.RateLimitStarterProperties;
import in.infusers.library.ratelimit.core.EndpointNormalizer;
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
class RateLimitingFilterTest {

    @Mock
    private RateLimitingService rateLimitingService;
    @Mock
    private SecurityAlertService securityAlertService;

    private RateLimitProperties rateLimitProperties;
    private IpSecurityProperties ipSecurityProperties;
    private RateLimitingFilter filter;

    private void init() {
        EndpointNormalizer endpointNormalizer = new EndpointNormalizer(new RateLimitStarterProperties());
        rateLimitProperties = new RateLimitProperties();
        ipSecurityProperties = new IpSecurityProperties();
        filter = new RateLimitingFilter(rateLimitingService, rateLimitProperties, endpointNormalizer, securityAlertService, ipSecurityProperties);
    }

    @Test
    void usesDefaultConfigWhenNoPathRuleMatches() throws Exception {
        init();
        when(rateLimitingService.isAllowed(anyString(), eq(1000), any())).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/unmapped/path");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void unlimitedConfigSkipsRateLimitCheckEntirely() throws Exception {
        init();
        Map<String, RateLimitProperties.RateLimitConfig> paths = new HashMap<>();
        RateLimitProperties.RateLimitConfig unlimited = new RateLimitProperties.RateLimitConfig();
        unlimited.setLimit(-1);
        unlimited.setDuration(Duration.ofMinutes(1));
        paths.put("admin", unlimited);
        rateLimitProperties.setPaths(paths);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/dashboard");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(rateLimitingService, never()).isAllowed(anyString(), anyInt(), any());
    }

    @Test
    void blocksAndAlertsWhenOverLimit() throws Exception {
        init();
        when(rateLimitingService.isAllowed(anyString(), anyInt(), any())).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/orders");
        request.addHeader("User-Agent", "Mozilla/5.0");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
        verify(securityAlertService).alertApiRateLimitBreach(anyString());
    }

    @Test
    void normalizesDynamicSegmentsBeforeMatchingPathRule() throws Exception {
        init();
        Map<String, RateLimitProperties.RateLimitConfig> paths = new HashMap<>();
        RateLimitProperties.RateLimitConfig config = new RateLimitProperties.RateLimitConfig();
        config.setLimit(5);
        config.setDuration(Duration.ofMinutes(1));
        paths.put("users", config);
        rateLimitProperties.setPaths(paths);
        when(rateLimitingService.isAllowed(anyString(), eq(5), any())).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/users/42");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verify(rateLimitingService).isAllowed(anyString(), eq(5), any());
    }

    @Test
    void keysByRealClientIpBehindAConfiguredTrustedProxyInsteadOfTheProxysOwnAddress() throws Exception {
        // Without this, every request arriving through a real reverse proxy would bucket
        // under the proxy's single IP, letting one abusive client exhaust the shared counter
        // for every legitimate user behind it.
        init();
        ipSecurityProperties.getTrustedProxies().add("10.0.0.1");
        when(rateLimitingService.isAllowed(anyString(), anyInt(), any())).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "1.2.3.4");
        request.setRequestURI("/unmapped/path");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        String expectedSafeIp = Base64.getUrlEncoder().encodeToString("1.2.3.4".getBytes(StandardCharsets.UTF_8));

        filter.doFilter(request, response, chain);

        verify(rateLimitingService).isAllowed(eq(expectedSafeIp + ":/unmapped/path"), anyInt(), any());
    }
}
