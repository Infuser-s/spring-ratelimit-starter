package in.infusers.library.ratelimit.core;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    @Test
    void usesXForwardedForWhenPeerIsTrustedProxy() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "1.2.3.4");

        assertThat(ClientIpResolver.resolve(request, List.of("10.0.0.1"))).isEqualTo("1.2.3.4");
    }

    @Test
    void trustedProxyCanBeConfiguredAsCidrRange() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.42");
        request.addHeader("X-Forwarded-For", "1.2.3.4");

        assertThat(ClientIpResolver.resolve(request, List.of("10.0.0.0/8"))).isEqualTo("1.2.3.4");
    }

    @Test
    void ignoresXForwardedForWhenPeerIsNotATrustedProxy() {
        // The whole point of the fix: an untrusted caller cannot spoof its IP just by
        // sending the header — including sending the exact example value from the README.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.9");
        request.addHeader("X-Forwarded-For", "127.0.0.1");

        assertThat(ClientIpResolver.resolve(request, List.of("10.0.0.1"))).isEqualTo("203.0.113.9");
    }

    @Test
    void ignoresXForwardedForWhenNoTrustedProxiesConfigured() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.9");
        request.addHeader("X-Forwarded-For", "1.2.3.4");

        assertThat(ClientIpResolver.resolve(request, Collections.emptyList())).isEqualTo("203.0.113.9");
    }

    @Test
    void takesFirstIpWhenXForwardedForHasMultiple() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8");

        assertThat(ClientIpResolver.resolve(request, List.of("10.0.0.1"))).isEqualTo("1.2.3.4");
    }

    @Test
    void fallsBackToXRealIpWhenXForwardedForUnknown() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "unknown");
        request.addHeader("X-Real-IP", "9.8.7.6");

        assertThat(ClientIpResolver.resolve(request, List.of("10.0.0.1"))).isEqualTo("9.8.7.6");
    }

    @Test
    void fallsBackToRemoteAddrWhenNoHeadersPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");

        assertThat(ClientIpResolver.resolve(request, List.of("10.0.0.1"))).isEqualTo("10.0.0.1");
    }

    @Test
    void fallsBackToRemoteAddrWhenHeadersEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.2");
        request.addHeader("X-Forwarded-For", "");

        assertThat(ClientIpResolver.resolve(request, List.of("10.0.0.2"))).isEqualTo("10.0.0.2");
    }

    @Test
    @SuppressWarnings("deprecation")
    void deprecatedSingleArgOverloadIsSafeByDefault() {
        // No trusted-proxy list can be passed via this overload, so it must never trust
        // forwarded headers from anyone — otherwise every existing caller of the old API
        // would still be spoofable.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.9");
        request.addHeader("X-Forwarded-For", "1.2.3.4");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("203.0.113.9");
    }
}
