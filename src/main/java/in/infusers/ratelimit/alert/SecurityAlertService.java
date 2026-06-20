package in.infusers.ratelimit.alert;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Rate-limits the alerts themselves — one breach notification per key per
 * hour, regardless of how many requests get blocked in that window.
 */
@Service
public class SecurityAlertService {

    private static final long ALERT_COOLDOWN_MS = 60 * 60 * 1000L;
    private static final Logger log = LoggerFactory.getLogger(SecurityAlertService.class);

    private final SecurityAlertListener listener;
    private final ConcurrentHashMap<String, Long> lastAlertTime = new ConcurrentHashMap<>();

    public SecurityAlertService(SecurityAlertListener listener) {
        this.listener = listener;
    }

    public void alertIpBreach(String ip, boolean suspicious, String reason) {
        String cooldownKey = "ip:" + ip + (suspicious ? ":suspicious" : "");
        if (!isCooledDown(cooldownKey)) return;

        String subject = suspicious
                ? "[SECURITY ALERT] Suspicious IP Blocked: " + ip
                : "[SECURITY ALERT] IP Rate Limit Breach: " + ip;
        String body = "IP Address : " + ip + "\n"
                + "Type      : " + (suspicious ? "Suspicious / Bot" : "High Request Rate") + "\n"
                + "Reason    : " + reason;

        log.warn("alertIpBreach() -> {}", subject);
        listener.onAlert(subject, body);
    }

    public void alertApiRateLimitBreach(String endpoint) {
        String cooldownKey = "api:" + endpoint;
        if (!isCooledDown(cooldownKey)) return;

        String subject = "[SECURITY ALERT] API Rate Limit Breach: " + endpoint;
        String body = "Endpoint  : " + endpoint + "\n"
                + "Type      : API Path Rate Limit\n"
                + "Action    : Request rejected with HTTP 429";

        log.warn("alertApiRateLimitBreach() -> {}", subject);
        listener.onAlert(subject, body);
    }

    private boolean isCooledDown(String key) {
        long now = System.currentTimeMillis();
        Long last = lastAlertTime.get(key);
        if (last != null && (now - last) < ALERT_COOLDOWN_MS) return false;
        lastAlertTime.put(key, now);
        return true;
    }
}
