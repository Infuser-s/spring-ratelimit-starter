package in.infusers.library.ratelimit.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Matches an IP address against an allowlist entry that is either an exact address
 * ("10.0.0.5") or a CIDR range ("10.0.0.0/8", including IPv6). No external state —
 * safe to call statically.
 */
public final class IpRangeMatcher {

    private IpRangeMatcher() {
    }

    public static boolean matches(String ip, String allowedEntry) {
        if (ip == null || ip.isBlank() || allowedEntry == null || allowedEntry.isBlank()) {
            return false;
        }

        String entry = allowedEntry.trim();
        try {
            if (!entry.contains("/")) {
                return InetAddress.getByName(ip.trim()).equals(InetAddress.getByName(entry));
            }

            String[] parts = entry.split("/", 2);
            InetAddress network = InetAddress.getByName(parts[0].trim());
            int prefixLength = Integer.parseInt(parts[1].trim());
            InetAddress candidate = InetAddress.getByName(ip.trim());

            return isInRange(candidate, network, prefixLength);
        } catch (UnknownHostException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Malformed entry or unparsable IP — fail closed (not a match) rather than throw,
            // since this runs on every request's whitelist/trusted-proxy check.
            return false;
        }
    }

    private static boolean isInRange(InetAddress candidate, InetAddress network, int prefixLength) {
        byte[] candidateBytes = candidate.getAddress();
        byte[] networkBytes = network.getAddress();

        // Different address families (IPv4 vs IPv6) can never match.
        if (candidateBytes.length != networkBytes.length) {
            return false;
        }
        if (prefixLength < 0 || prefixLength > candidateBytes.length * 8) {
            return false;
        }

        int fullBytes = prefixLength / 8;
        int remainingBits = prefixLength % 8;

        for (int i = 0; i < fullBytes; i++) {
            if (candidateBytes[i] != networkBytes[i]) {
                return false;
            }
        }

        if (remainingBits > 0) {
            int mask = 0xFF << (8 - remainingBits) & 0xFF;
            if ((candidateBytes[fullBytes] & mask) != (networkBytes[fullBytes] & mask)) {
                return false;
            }
        }

        return true;
    }
}
