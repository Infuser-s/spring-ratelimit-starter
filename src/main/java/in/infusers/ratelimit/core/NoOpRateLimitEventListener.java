package in.infusers.ratelimit.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(RateLimitEventListener.class)
public class NoOpRateLimitEventListener implements RateLimitEventListener {

    private static final Logger log = LoggerFactory.getLogger(NoOpRateLimitEventListener.class);

    @Override
    public void onRateLimitEvent(String type, String identifier, String reason, String ipAddress, String userAgent) {
        log.warn("RateLimit exceeded. type={} identifier={} reason={} ip={} userAgent={}",
                type, identifier, reason, ipAddress, userAgent);
    }
}
