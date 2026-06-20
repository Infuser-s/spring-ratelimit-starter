package in.infusers.library.ratelimit.filter;

import java.io.IOException;
import java.time.Duration;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import in.infusers.library.ratelimit.alert.SecurityAlertService;
import in.infusers.library.ratelimit.config.IpSecurityProperties;
import in.infusers.library.ratelimit.core.ClientIpResolver;
import in.infusers.library.ratelimit.core.RateLimitingService;

/** IP-based security rate limiter. First in the filter chain (Order 1). */
@Component
@Order(1)
public class IPSecurityRateLimitFilter implements Filter {

    private final RateLimitingService rateLimitingService;
    private final IpSecurityProperties ipSecurityProperties;
    private final SecurityAlertService securityAlertService;

    public IPSecurityRateLimitFilter(RateLimitingService rateLimitingService,
                                      IpSecurityProperties ipSecurityProperties,
                                      SecurityAlertService securityAlertService) {
        this.rateLimitingService = rateLimitingService;
        this.ipSecurityProperties = ipSecurityProperties;
        this.securityAlertService = securityAlertService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String clientIp = ClientIpResolver.resolve(httpRequest);

        if (shouldBypassRateLimit(clientIp, httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        int maxRequests = ipSecurityProperties.getMaxRequestsPerMinute();
        int suspiciousThreshold = ipSecurityProperties.getSuspiciousThreshold();

        boolean isSuspicious = determineSuspiciousActivity(httpRequest);
        int effectiveLimit = isSuspicious ? suspiciousThreshold : maxRequests;

        String rateLimitKey = "ip_security:" + clientIp + (isSuspicious ? ":suspicious" : "");

        if (!rateLimitingService.isAllowed(rateLimitKey, effectiveLimit, Duration.ofMinutes(1))) {
            String reason = isSuspicious ? "Suspicious activity" : "High request rate";
            rateLimitingService.logRateLimitEvent("IP_SECURITY", clientIp, reason, clientIp, httpRequest.getHeader("User-Agent"));
            securityAlertService.alertIpBreach(clientIp, isSuspicious, reason);

            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setHeader("X-RateLimit-Type", "Security");
            httpResponse.setHeader("Content-Type", "text/plain");
            httpResponse.getWriter().write("Rate limit exceeded for security reasons");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean determineSuspiciousActivity(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String requestUri = request.getRequestURI();

        if (userAgent == null || userAgent.trim().isEmpty()) {
            return true;
        }

        String[] suspiciousAgents = {"sqlmap", "nikto", "nmap", "masscan", "zap"};
        for (String suspiciousAgent : suspiciousAgents) {
            if (userAgent.toLowerCase().contains(suspiciousAgent)) {
                return true;
            }
        }

        String[] suspiciousUris = {"/admin", "/.env", "/wp-admin", "/phpmyadmin"};
        for (String suspiciousUri : suspiciousUris) {
            if (requestUri.contains(suspiciousUri)) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldBypassRateLimit(String clientIp, HttpServletRequest request) {
        if (ipSecurityProperties.getWhitelistedIps().contains(clientIp)) {
            return true;
        }
        if (isIpInCidrRange(clientIp)) {
            return true;
        }
        if (isExemptUserAgent(request)) {
            return true;
        }
        if (hasValidInternalApiKey(request)) {
            return true;
        }
        return false;
    }

    private boolean isExemptUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (!StringUtils.hasText(userAgent)) {
            return false;
        }
        return ipSecurityProperties.getExemptUserAgents().stream()
                .anyMatch(exemptAgent -> userAgent.contains(exemptAgent));
    }

    private boolean hasValidInternalApiKey(HttpServletRequest request) {
        String apiKeyHeader = ipSecurityProperties.getInternalTools().getApiKeyHeader();
        String providedApiKey = request.getHeader(apiKeyHeader);

        if (!StringUtils.hasText(providedApiKey)) {
            return false;
        }
        return ipSecurityProperties.getInternalTools().getValidApiKeys().contains(providedApiKey);
    }

    private boolean isIpInCidrRange(String clientIp) {
        // CIDR matching not yet implemented — whitelistedCidrs is reserved for this.
        return false;
    }
}
