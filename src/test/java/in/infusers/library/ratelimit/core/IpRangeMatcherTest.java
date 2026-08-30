package in.infusers.library.ratelimit.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IpRangeMatcherTest {

    @Test
    void matchesExactIp() {
        assertThat(IpRangeMatcher.matches("10.0.0.5", "10.0.0.5")).isTrue();
        assertThat(IpRangeMatcher.matches("10.0.0.5", "10.0.0.6")).isFalse();
    }

    @Test
    void matchesIpv4Cidr() {
        assertThat(IpRangeMatcher.matches("10.1.2.3", "10.0.0.0/8")).isTrue();
        assertThat(IpRangeMatcher.matches("11.1.2.3", "10.0.0.0/8")).isFalse();
        assertThat(IpRangeMatcher.matches("192.168.1.200", "192.168.1.0/24")).isTrue();
        assertThat(IpRangeMatcher.matches("192.168.2.1", "192.168.1.0/24")).isFalse();
    }

    @Test
    void matchesCidrOnNonByteAlignedPrefix() {
        // 192.168.1.128/25 covers .128-.255 only
        assertThat(IpRangeMatcher.matches("192.168.1.200", "192.168.1.128/25")).isTrue();
        assertThat(IpRangeMatcher.matches("192.168.1.100", "192.168.1.128/25")).isFalse();
    }

    @Test
    void matchesIpv6Cidr() {
        assertThat(IpRangeMatcher.matches("::1", "::1/128")).isTrue();
        assertThat(IpRangeMatcher.matches("2001:db8::1", "2001:db8::/32")).isTrue();
        assertThat(IpRangeMatcher.matches("2001:db9::1", "2001:db8::/32")).isFalse();
    }

    @Test
    void neverMatchesAcrossAddressFamilies() {
        assertThat(IpRangeMatcher.matches("10.0.0.1", "::1/128")).isFalse();
    }

    @Test
    void failsClosedOnMalformedInput() {
        assertThat(IpRangeMatcher.matches("not-an-ip", "10.0.0.0/8")).isFalse();
        assertThat(IpRangeMatcher.matches("10.0.0.1", "10.0.0.0/not-a-prefix")).isFalse();
        assertThat(IpRangeMatcher.matches(null, "10.0.0.0/8")).isFalse();
        assertThat(IpRangeMatcher.matches("10.0.0.1", null)).isFalse();
    }
}
