# spring-ratelimit-starter — Catalog

Redis-backed rate limiting for Spring Boot, published to Maven Central as `in.infusers.library:spring-ratelimit-starter`. Table format: [DOC_FORMAT.md](https://github.com/Infuser-s/infusers-hangar/blob/master/docs/engineering/DOC_FORMAT.md).

| Feature | Status | Summary | Doc |
|---|---|---|---|
| Spring Boot auto-configuration | shipped | Registers filters and services via standard autoconfiguration when a StringRedisTemplate bean is present; no annotations needed | README.md |
| Per-route rate limits | shipped | RateLimitingFilter applies limit and duration per path prefix from infusers.ratelimit.rules; -1 means unlimited | README.md |
| Dynamic path normalization | shipped | Segments such as /users/42 and /users/43 share one bucket (/users/{variable}); excluded-paths opt out | README.md |
| IP security filter | shipped | IPSecurityRateLimitFilter blocks abusive IPs with max-requests-per-minute and a suspicious-traffic threshold | README.md |
| IP and CIDR whitelisting | shipped | Whitelist by IP or CIDR range, exempt user agents, or an internal API key header | README.md |
| Trusted proxy client-IP resolution | shipped | Resolves the real client IP behind reverse proxies and load balancers using a trusted-proxies list | README.md |
| RateLimitingService | shipped | Redis INCR plus EXPIRE counter, usable directly outside the two filters | README.md |
| Security alert hooks | shipped | Cooldown-gated breach alerts (one per key per hour) routed to your own SecurityAlertListener; no-op by default | README.md |
| Rate-limit event listener | shipped | Pluggable RateLimitEventListener callback for limit events; no-op by default | README.md |
| Maven Central publishing | shipped | Signed release pipeline (Jenkinsfile_Promote) with version-exists check and auto-publish on Central | PUBLISHING.md |
