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
        assertThat(properties.getExemptUserAgents()).isEmpty();
        assertThat(properties.getInternalTools().getApiKeyHeader()).isEqualTo("X-Internal-API-Key");
        assertThat(properties.getInternalTools().getValidApiKeys()).isEmpty();
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
