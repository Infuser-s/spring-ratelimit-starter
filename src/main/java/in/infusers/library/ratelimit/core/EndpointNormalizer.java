package in.infusers.library.ratelimit.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.infusers.library.ratelimit.config.RateLimitStarterProperties;

/**
 * Collapses dynamic path segments (numeric IDs, emails) to a single
 * {@code {variable}} token so per-path rate limits group requests like
 * {@code /users/42} and {@code /users/43} under one bucket. Paths matching
 * a configured exclusion prefix are returned unchanged.
 */
public class EndpointNormalizer {

    private static final Pattern SEGMENT_PATTERN = Pattern.compile("/([^/]+?)(?=/|$)");
    private static final Pattern NUMERIC = Pattern.compile("\\d+");
    private static final Pattern EMAIL = Pattern.compile("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    private final RateLimitStarterProperties properties;

    public EndpointNormalizer(RateLimitStarterProperties properties) {
        this.properties = properties;
    }

    public String normalize(String uri) {
        for (String excluded : properties.getExcludedPaths()) {
            if (uri.startsWith(excluded)) {
                return uri;
            }
        }

        Matcher matcher = SEGMENT_PATTERN.matcher(uri);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String segment = matcher.group(1);
            String replacement = (NUMERIC.matcher(segment).matches() || EMAIL.matcher(segment).matches())
                    ? "/{variable}"
                    : matcher.group(0);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
