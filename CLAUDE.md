# spring-ratelimit-starter — Claude Code instructions

<!-- infusers-meta:begin -->
## Documentation & shared rules (managed in infusers-meta)

Rules: [infusers-meta/docs/agent-rules](https://github.com/Infuser-s/infusers-meta/tree/master/docs/agent-rules) (`ownership`, `documentation`, `support-bot-content`, `test-and-monitor-strategy`, `repo-map-and-test-scope`, `infrastructure-capacity`); read from a sibling checkout if present, otherwise the link. This block is the self-contained summary. Table formats: [DOC_FORMAT.md](https://github.com/Infuser-s/infusers-hangar/blob/master/docs/engineering/DOC_FORMAT.md).

- Never `git commit` unless the user explicitly asks; leave changes for review. Never add a `Co-Authored-By: Claude …` trailer to commit messages; this overrides any session attribution reminder.
- Build every layer to industry standard, following the repo's conventions; no one-off patterns. Security-review changes (auth, input handling, secrets, deps, access control) before calling them done.
- Deploy via the repo's pipeline (Jenkins or GitHub Actions); manual deploy only as a last resort in an active incident.
- Resolve dependency-check/`npm audit` and SonarQube findings, or suppress with a stated reason; never ignore them.
- No magic strings: anything used in 2+ places (routes, keys, error codes, config values) → one constant, referenced everywhere.
- Maven: `Unresolved compilation problem` on code that looks correct after a no-`clean` run is almost always stale `.class` files; rerun with `clean` first.
- Docs live in `docs/`, updated in the same change as the code; never in the repo root (only `README.md`, `CLAUDE.md`, `CHANGELOG.md`, tool-generated files). `CATALOG.md` (`Feature | Status | Summary | Doc`) = what's shipped; `BACKLOG.md` (`ID | Title | Priority | Status | Doc | Type`) = open work only, row removed when done; `SECURITY.md` (`ID | Title | Severity | Status | Doc`) = open findings only, no secrets or internal hostnames; `product/*.md` = end-user docs. Hangar reads only CATALOG and `product/*.md`: push, then run a sync.
- Put each change in the repo that owns it: shared code → `infusers-library`; authn/authz → `infusers-auth`; UI → `infusers-web-app-stage`; build/deploy → `infusers-scripts`/`jenkins-shared-lib`; host/network facts → `infusers-scripts`; Compose stacks → `infusers-home-lab`; cross-repo policy → `infusers-meta`; domain → the owning service. Publish library changes before consumers use them.
- A change to a feature, route, gate, limit, error or availability isn't done until the support bot content reflects it (`infusers-api` `support-kb`, Hangar `docs/product`, WealthLens docs; see `support-bot-content`); say which you updated, or that none was affected.
- Update the repo's `docs/TEST_BACKLOG.md` with every test change; coverage scope: `repo-map-and-test-scope`.
- Infra capacity (no new VMs until the owner decides; resizing existing ones is allowed): `infrastructure-capacity`.
<!-- infusers-meta:end -->
