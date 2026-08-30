package in.infusers.library.ratelimit.core;

import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the real client IP from common proxy headers, falling back to
 * the raw connection address. No external state — safe to call statically.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    /**
     * @deprecated previously honored X-Forwarded-For/X-Real-IP unconditionally, from any
     * peer, which let any direct caller spoof its apparent IP and bypass IP-based
     * whitelisting/rate-limiting entirely. This overload now delegates to
     * {@link #resolve(HttpServletRequest, List)} with an empty trusted-proxy list, so it
     * simply returns {@code request.getRemoteAddr()} — safe, but ignores forwarded headers
     * even behind a real reverse proxy. Use the two-argument overload with your proxy's
     * address (or CIDR) so forwarded headers are honored only from a peer you actually trust.
     */
    @Deprecated
    public static String resolve(HttpServletRequest request) {
        return resolve(request, Collections.emptyList());
    }

    /**
     * Resolves the client IP, honoring X-Forwarded-For/X-Real-IP only when the request's
     * immediate peer ({@code request.getRemoteAddr()}) matches one of {@code trustedProxies}
     * (exact IP or CIDR, e.g. "10.0.0.1" or "10.0.0.0/8"). Any other caller — including one
     * that spoofs those headers directly — gets resolved to its raw connection address
     * instead, since the headers are otherwise attacker-controlled.
     */
    public static String resolve(HttpServletRequest request, List<String> trustedProxies) {
        String remoteAddr = request.getRemoteAddr();

        if (!isFromTrustedProxy(remoteAddr, trustedProxies)) {
            return remoteAddr;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return remoteAddr;
        }

        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    private static boolean isFromTrustedProxy(String remoteAddr, List<String> trustedProxies) {
        if (trustedProxies == null || trustedProxies.isEmpty()) {
            return false;
        }
        return trustedProxies.stream().anyMatch(proxy -> IpRangeMatcher.matches(remoteAddr, proxy));
    }
}
