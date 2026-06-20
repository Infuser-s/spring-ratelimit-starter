package in.infusers.library.ratelimit.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registered as a {@code @Bean} from {@code RateLimitAutoConfiguration} rather than
 * {@code @Component}-scanned — {@code @ConditionalOnMissingBean} does not reliably
 * evaluate on component-scanned classes (no competing bean still failed to register
 * in testing). Keep this a plain class.
 */
public class NoOpSecurityAlertListener implements SecurityAlertListener {

    private static final Logger log = LoggerFactory.getLogger(NoOpSecurityAlertListener.class);

    @Override
    public void onAlert(String subject, String body) {
        log.warn("{}\n{}", subject, body);
    }
}
