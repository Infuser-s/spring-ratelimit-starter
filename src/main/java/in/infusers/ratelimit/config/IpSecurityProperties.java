package in.infusers.ratelimit.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "infusers.ratelimit.ip")
public class IpSecurityProperties {

    private int maxRequestsPerMinute = 100;
    private int suspiciousThreshold = 20;
    private List<String> whitelistedIps = new ArrayList<>();
    private List<String> whitelistedCidrs = new ArrayList<>();
    private List<String> exemptUserAgents = new ArrayList<>();
    private InternalTools internalTools = new InternalTools();

    public int getMaxRequestsPerMinute() {
        return maxRequestsPerMinute;
    }

    public void setMaxRequestsPerMinute(int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }

    public int getSuspiciousThreshold() {
        return suspiciousThreshold;
    }

    public void setSuspiciousThreshold(int suspiciousThreshold) {
        this.suspiciousThreshold = suspiciousThreshold;
    }

    public List<String> getWhitelistedIps() {
        return whitelistedIps;
    }

    public void setWhitelistedIps(List<String> whitelistedIps) {
        this.whitelistedIps = whitelistedIps;
    }

    public List<String> getWhitelistedCidrs() {
        return whitelistedCidrs;
    }

    public void setWhitelistedCidrs(List<String> whitelistedCidrs) {
        this.whitelistedCidrs = whitelistedCidrs;
    }

    public List<String> getExemptUserAgents() {
        return exemptUserAgents;
    }

    public void setExemptUserAgents(List<String> exemptUserAgents) {
        this.exemptUserAgents = exemptUserAgents;
    }

    public InternalTools getInternalTools() {
        return internalTools;
    }

    public void setInternalTools(InternalTools internalTools) {
        this.internalTools = internalTools;
    }

    public static class InternalTools {
        private String apiKeyHeader = "X-Internal-API-Key";
        private List<String> validApiKeys = new ArrayList<>();

        public String getApiKeyHeader() {
            return apiKeyHeader;
        }

        public void setApiKeyHeader(String apiKeyHeader) {
            this.apiKeyHeader = apiKeyHeader;
        }

        public List<String> getValidApiKeys() {
            return validApiKeys;
        }

        public void setValidApiKeys(List<String> validApiKeys) {
            this.validApiKeys = validApiKeys;
        }
    }
}
