# spring-ratelimit-starter

Redis-backed rate limiting for Spring Boot — per-route limits, IP-level security filtering, pluggable event/alert hooks. Auto-configures via Spring Boot's standard autoconfiguration mechanism; no `@Import` or manual bean wiring needed.

## What it does

- **`IPSecurityRateLimitFilter`** (servlet filter, order 1) — blocks abusive IPs before they reach your app. Whitelists by IP, exempt user agents, or an internal API key header. Flags suspicious traffic (missing user agent, known scanner agents, common attack paths) for a tighter threshold.
- **`RateLimitingFilter`** (servlet filter, order 2) — per-route limits, configured by path prefix. Dynamic segments (`/users/42`, `/users/43`) normalize to one bucket (`/users/{variable}`) so you don't need a rule per ID.
- **`RateLimitingService`** — the underlying Redis counter (`INCR` + `EXPIRE` on first hit), usable directly if you need rate limiting outside the two filters.
- **`SecurityAlertService`** — cooldown-gated breach notifications (one alert per key per hour), routed through your own `SecurityAlertListener`.

## Install

```xml
<dependency>
    <groupId>in.infusers.library</groupId>
    <artifactId>spring-ratelimit-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

Requires a `StringRedisTemplate` bean on the classpath (e.g. `spring-boot-starter-data-redis`) — autoconfiguration is conditional on it.

## Configure

```yaml
infusers:
  ratelimit:
    key-prefix: "myapp:ratelimit:"        # default: "ratelimit:"
    excluded-paths:                        # paths exempt from dynamic-segment normalization
      - /version
      - /swagger-ui
    rules:
      paths:
        users_login:                       # underscores become slashes: /users/login
          limit: 10
          duration: 1m
        admin:
          limit: -1                        # -1 = unlimited
          duration: 1m
    ip:
      max-requests-per-minute: 100
      suspicious-threshold: 20
      whitelisted-ips:
        - 127.0.0.1
      exempt-user-agents:
        - InternalHealthCheck
      internal-tools:
        api-key-header: X-Internal-API-Key
        valid-api-keys:
          - your-internal-key
```

## Wire up your own alerting

By default, breaches and rate-limit events just log via SLF4J (`NoOpSecurityAlertListener`, `NoOpRateLimitEventListener`). Register your own bean to route them anywhere else:

```java
@Component
public class EmailSecurityAlertListener implements SecurityAlertListener {
    @Override
    public void onAlert(String subject, String body) {
        // send email, Slack, PagerDuty, whatever
    }
}

@Component
public class AuditRateLimitEventListener implements RateLimitEventListener {
    @Override
    public void onRateLimitEvent(String type, String identifier, String reason, String ipAddress, String userAgent) {
        // forward to your audit/observability system
    }
}
```

Registering either bean overrides the no-op default — `@ConditionalOnMissingBean` backs off automatically.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
