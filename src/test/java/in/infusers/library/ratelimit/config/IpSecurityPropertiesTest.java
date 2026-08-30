package in.infusers.library.ratelimit.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IpSecurityPropertiesTest {

    @Test
    void defaultsAreSensible() {
        IpSecurityProperties properties = new IpSecurityProperties();

        assertThat(properties.getMaxRequestsPerMinute()).isEqualTo(100);
        assertThat(properties.getSuspiciousThreshold()).isEqualTo(20);
        assertThat(properties.getWhitelistedIps()).isEmpty();
        assertThat(properties.getWhitelistedCidrs()).isEmpty();
        assertThat(properties.getExemptUserAgents()).isEmpty();
        assertThat(properties.getInternalTools().getApiKeyHeader()).isEqualTo("X-Internal-API-Key");
        assertThat(properties.getInternalTools().getValidApiKeys()).isEmpty();
        // Empty by default is the safe default — see ClientIpResolver: forwarded headers are
        // only honored from a peer explicitly listed here.
        assertThat(properties.getTrustedProxies()).isEmpty();
    }

    @Test
    void trustedProxiesCanBeConfigured() {
        IpSecurityProperties properties = new IpSecurityProperties();
        properties.setTrustedProxies(java.util.List.of("10.0.0.1", "192.168.0.0/16"));

        assertThat(properties.getTrustedProxies()).containsExactly("10.0.0.1", "192.168.0.0/16");
    }

    @Test
    void settersOverrideDefaults() {
        IpSecurityProperties properties = new IpSecurityProperties();
        properties.setMaxRequestsPerMinute(50);
        properties.setSuspiciousThreshold(5);

        assertThat(properties.getMaxRequestsPerMinute()).isEqualTo(50);
        assertThat(properties.getSuspiciousThreshold()).isEqualTo(5);
    }
}
