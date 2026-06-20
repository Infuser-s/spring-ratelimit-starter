package in.infusers.library.ratelimit.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(SecurityAlertListener.class)
public class NoOpSecurityAlertListener implements SecurityAlertListener {

    private static final Logger log = LoggerFactory.getLogger(NoOpSecurityAlertListener.class);

    @Override
    public void onAlert(String subject, String body) {
        log.warn("{}\n{}", subject, body);
    }
}
