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
    <version>0.1.1</version>
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
      trusted-proxies:            # required to make whitelisting/rate-limiting IP-aware at all
        - 127.0.0.1               # behind a reverse proxy — see note below
```

### `trusted-proxies` — read this before deploying behind any reverse proxy

`whitelisted-ips`/`whitelisted-cidrs` and the per-route rate limiter key requests by the
caller's IP. If your app sits behind a reverse proxy (nginx, a load balancer, Cloudflare, …),
the only IP this library sees directly is the proxy's own — the real client IP arrives via the
`X-Forwarded-For`/`X-Real-IP` headers instead, and **those headers are just as easy for an
attacker to set as the proxy is**. Without `trusted-proxies` configured, this library does the
safe thing and ignores those headers entirely, using the raw connection address — which means
every request behind a proxy is (correctly, if unhelpfully) treated as coming from the proxy
itself.

Set `trusted-proxies` to the proxy's own IP (or CIDR range) to opt in: the header is honored
**only** when the immediate connection is from an address in this list. A client that isn't
connecting through your proxy — including one that sends
`X-Forwarded-For: 127.0.0.1` directly, matching the `whitelisted-ips` example above — is not
affected by this setting and cannot spoof its way past the whitelist or the rate limiter. Leave
`trusted-proxies` empty (the default) if you have no reverse proxy in front of this app, or if
your proxy already strips inbound `X-Forwarded-For`/`X-Real-IP` headers before adding its own.

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
