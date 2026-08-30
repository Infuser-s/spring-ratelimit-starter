package in.infusers.library.ratelimit.config;

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
    // Deliberately empty by default: X-Forwarded-For/X-Real-IP are only honored when the
    // request's immediate peer matches one of these entries (exact IP or CIDR). Otherwise
    // those headers are attacker-controlled and this app sees only request.getRemoteAddr().
    private List<String> trustedProxies = new ArrayList<>();

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

    public List<String> getTrustedProxies() {
        return trustedProxies;
    }

    public void setTrustedProxies(List<String> trustedProxies) {
        this.trustedProxies = trustedProxies;
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
