package in.infusers.ratelimit.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import in.infusers.ratelimit.alert.SecurityAlertService;
import in.infusers.ratelimit.config.RateLimitProperties;
import in.infusers.ratelimit.core.EndpointNormalizer;
import in.infusers.ratelimit.core.RateLimitingService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Per-route rate limiter. Second in the filter chain (Order 2) — runs after IP-level security. */
@Component
@Order(2)
public class RateLimitingFilter extends HttpFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final RateLimitingService rateLimitingService;
    private final RateLimitProperties rateLimitProperties;
    private final EndpointNormalizer endpointNormalizer;
    private final SecurityAlertService securityAlertService;
    private final RateLimitProperties.RateLimitConfig defaultConfig;

    public RateLimitingFilter(RateLimitingService rateLimitingService,
                               RateLimitProperties rateLimitProperties,
                               EndpointNormalizer endpointNormalizer,
                               SecurityAlertService securityAlertService) {
        this.rateLimitingService = rateLimitingService;
        this.rateLimitProperties = rateLimitProperties;
        this.endpointNormalizer = endpointNormalizer;
        this.securityAlertService = securityAlertService;
        this.defaultConfig = createDefaultConfig();
    }

    private RateLimitProperties.RateLimitConfig createDefaultConfig() {
        RateLimitProperties.RateLimitConfig config = new RateLimitProperties.RateLimitConfig();
        config.setLimit(1000);
        config.setDuration(Duration.ofMinutes(1));
        return config;
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String requestURI = endpointNormalizer.normalize(request.getRequestURI());
        RateLimitProperties.RateLimitConfig config = getConfigForPath(requestURI);

        String clientIp = request.getRemoteAddr();
        String safeIp = Base64.getUrlEncoder().encodeToString(clientIp.getBytes(StandardCharsets.UTF_8));
        String key = safeIp + ":" + requestURI;

        if (!config.isUnlimited() && !rateLimitingService.isAllowed(key, config.getLimit(), config.getDuration())) {
            log.warn("doFilter() RateLimit exceeded. requestURI={} :: {}", requestURI, config);
            rateLimitingService.logRateLimitEvent("API_PATH", requestURI, "Path rate limit exceeded", clientIp, request.getHeader("User-Agent"));
            securityAlertService.alertApiRateLimitBreach(requestURI);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-RateLimit-Type", "Security");
            response.getWriter().write("Rate limit exceeded for security reasons");
            return;
        }
        chain.doFilter(request, response);
    }

    private RateLimitProperties.RateLimitConfig getConfigForPath(String path) {
        if (rateLimitProperties.getPaths() == null) {
            return defaultConfig;
        }
        return rateLimitProperties.getPaths().keySet().stream()
                .filter(path::startsWith)
                .map(rateLimitProperties.getPaths()::get)
                .findFirst()
                .orElse(defaultConfig);
    }
}
