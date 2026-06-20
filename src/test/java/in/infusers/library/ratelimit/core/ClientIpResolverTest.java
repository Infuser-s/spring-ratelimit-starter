package in.infusers.library.ratelimit.core;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    @Test
    void usesXForwardedForWhenPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "1.2.3.4");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("1.2.3.4");
    }

    @Test
    void takesFirstIpWhenXForwardedForHasMultiple() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("1.2.3.4");
    }

    @Test
    void fallsBackToXRealIpWhenXForwardedForUnknown() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "unknown");
        request.addHeader("X-Real-IP", "9.8.7.6");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("9.8.7.6");
    }

    @Test
    void fallsBackToRemoteAddrWhenNoHeadersPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("10.0.0.1");
    }

    @Test
    void fallsBackToRemoteAddrWhenHeadersEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "");
        request.setRemoteAddr("10.0.0.2");

        assertThat(ClientIpResolver.resolve(request)).isEqualTo("10.0.0.2");
    }
}
