package in.infusers.library.ratelimit.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registered as a {@code @Bean} from {@code RateLimitAutoConfiguration} rather than
 * {@code @Component}-scanned — {@code @ConditionalOnMissingBean} does not reliably
 * evaluate on component-scanned classes (no competing bean still failed to register
 * in testing). Keep this a plain class.
 */
public class NoOpRateLimitEventListener implements RateLimitEventListener {

    private static final Logger log = LoggerFactory.getLogger(NoOpRateLimitEventListener.class);

    @Override
    public void onRateLimitEvent(String type, String identifier, String reason, String ipAddress, String userAgent) {
        log.warn("RateLimit exceeded. type={} identifier={} reason={} ip={} userAgent={}",
                type, identifier, reason, ipAddress, userAgent);
    }
}
