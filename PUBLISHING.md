# Publishing to Maven Central

## Namespace

Verified namespace on Sonatype Central Portal: **`in.infusers.library`** — exact string, not just `in.infusers`.

Central requires a published groupId to equal the verified namespace or be a sub-package of it (`in.infusers.library.*`). It does **not** extend to sibling prefixes — `in.infusers.ratelimit` was rejected with `Namespace 'in.infusers.ratelimit' is not allowed` even though `in.infusers.library` was verified on the same account, because `in.infusers` itself was never separately verified. If a future module needs a new groupId, it must literally start with `in.infusers.library`.

## Account

- Central Portal username: `infuser`
- GPG signing key: `0xB239A086FC8D59CB` (see `<keyname>` in `pom.xml`) — rotated 2026-08-30; if the Jenkins `gpg-public-key`/`gpg-private-key` credentials are ever rotated again, update this and `pom.xml` together, or signing fails with `gpg: skipped "<keyname>": No secret key`

Credentials live in Jenkins, not here:

| Jenkins credential ID | Type | Used for |
|---|---|---|
| `sonatype-maven-central-login` | username/password | Central Portal User Token (not the account login password) |
| `gpg-public-key` | file | imported before signing |
| `gpg-private-key` | file | imported before signing |
| `gpg-key-passphrase` | string | unlocks the private key during `gpg:sign` |

## Publish flow (`Jenkinsfile_Promote`)

1. Bump `<version>` in `pom.xml` first. The "Version check in Maven Central" stage hard-fails the build if that version already exists — Central publishes are immutable, there's no overwrite.
2. Must run with `-Pprod`. The `dev` profile is `activeByDefault=true` with `skipSigning=true` — a plain `mvn deploy` (no profile flag) will build and upload successfully but silently skip GPG signing, and Central rejects the deployment with `Missing signature for file: ...` on all four artifacts (jar, pom, sources, javadoc).
3. `central-publishing-maven-plugin` has `autoPublish=true` set. Once Central finishes validating the uploaded bundle, it publishes automatically — no manual "Publish" click needed on central.sonatype.com. Status flow: `PUBLISHING` → `PUBLISHED`, usually within a few minutes. Don't click **Drop** on a deployment unless you actually want to cancel it.

## Known open issue

GitHub webhook config currently causes a `stage`-branch push to also trigger the `Promote` (prod) Jenkins job, not just the dev build job. Not yet fixed — needs the GitHub webhook list checked for duplicates and the Promote job's branch filter scoped so it only fires on the intended branch/tag. Until fixed, watch Jenkins after every stage push to make sure Promote didn't fire unintentionally.

## 0.1.0 → 0.1.1 — startup-crash bug, fixed

0.1.0 shipped with zero tests. Writing a test suite afterward (45 tests, JUnit5 + Mockito + Spring's `ApplicationContextRunner`) surfaced a real bug: `NoOpRateLimitEventListener`/`NoOpSecurityAlertListener` were `@Component`-scanned classes with class-level `@ConditionalOnMissingBean`. Confirmed via a bare `AnnotationConfigApplicationContext` probe that this never actually registers the bean — **even with zero competing beans**. Any 0.1.0 consumer that doesn't supply its own `RateLimitEventListener`/`SecurityAlertListener` bean gets `UnsatisfiedDependencyException` at startup.

Fixed in 0.1.1: both no-op defaults moved to explicit `@Bean @ConditionalOnMissingBean` methods inside `RateLimitAutoConfiguration`, alongside the existing `endpointNormalizer` bean method. Verified working via the same `ApplicationContextRunner` test.

**0.1.0 can't be patched in place — Central publishes are immutable.** If anything is still pinned to 0.1.0, bump to `0.1.1` or later. infusers-library was never affected (it always supplied its own listener beans), but any zero-config consumer would be.

Lesson for future starters built on this pattern: conditional-default beans must be `@Bean` methods in the autoconfiguration class, never `@Component` + class-level `@Conditional*`.

## Tests

45 tests (JUnit5 + Mockito + AssertJ + Spring's `ApplicationContextRunner`), covering all 12 non-interface classes. `mvn test` requires JDK 17 or 21 — Mockito's inline mock maker doesn't yet support JDK 25's bytecode on this machine's toolchain; `JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn test` works around it.

Tests run on every `stage` push (`Jenkinsfile` → `BuildUtils.mvnInstall(..., 'dev')`, no skip flag for non-prod profiles). They do **not** run during the actual Promote/publish step — both `mvnInstall(..., 'prod')` and the final `mvn deploy` pass `-DskipTests`/`-Dmaven.test.skip=true`, trusting that the dev pipeline already gated the commit being promoted.

## Config that lives outside this repo

This repo has no knowledge of any specific consumer's environment — don't hardcode real values here.

- Admin email recipients for a `SecurityAlertListener` implementation are an infusers-library-side concern, configured via `infusers.contact.admin-email` in `infusers-shared-config-repo` (`config/application.properties`), not in this repo.
- `infusers-library` is the reference consumer: it implements `RateLimitEventListener`/`SecurityAlertListener` itself (`com.infusers.core.ratelimit.AuditRateLimitEventListener`, `EmailSecurityAlertListener`) to route events into its existing audit-trail and email infra. Use that as the template for wiring this starter into another internal-infra-aware project.

## Pattern for extracting future modules out of infusers-library

1. Copy the classes into the new repo.
2. Replace any call into infusers-library-internal infra (audit service, email utility, custom logger, etc.) with a small pluggable listener interface owned by the new repo.
3. Provide a no-op default implementation (`@ConditionalOnMissingBean`) so the new repo works standalone with zero config.
4. Back in infusers-library, implement that listener interface to forward into the real internal infra — this is what preserves existing production behavior (audit logging, email alerts) after the swap.
