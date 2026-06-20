package in.infusers.library.ratelimit.alert;

/**
 * Callback for rate-limit security breaches (IP blocks, API path breaches).
 * Implement and register a bean of this type to wire up email/Slack/PagerDuty
 * notifications. {@link NoOpSecurityAlertListener} is the default.
 */
public interface SecurityAlertListener {

    void onAlert(String subject, String body);
}
