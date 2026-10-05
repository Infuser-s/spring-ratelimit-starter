# Instructions

Build every layer (UI, DB, backend, API, …) to industry standard, following its established conventions; no one-off patterns.

Security-review your changes where applicable (e.g. auth, input handling, secrets, deps, access control) before calling them done.

<!-- infusers-meta:begin -->
## Documentation & shared rules (managed in infusers-meta)

Shared rules: [infusers-meta/docs/agent-rules](https://github.com/Infuser-s/infusers-meta/tree/master/docs/agent-rules) (`ownership.md`, `support-bot-content.md`, `documentation.md`). Read them from a sibling checkout if present, otherwise from that link; this block is the self-contained summary and works on any machine or user. Doc table formats: [infusers-hangar DOC_FORMAT.md](https://github.com/bro-labs/infusers-hangar/blob/master/docs/engineering/DOC_FORMAT.md).

- Docs live in `docs/` and are updated in the same change as the code. `docs/CATALOG.md` (pipe table `Feature | Status | Summary | Doc`) = what's shipped; `docs/BACKLOG.md` (`ID | Title | Priority | Status | Doc | Type`) = open work only, row removed in the change that finishes it; `docs/SECURITY.md` (`ID | Title | Severity | Status | Doc`) = open findings only, no secrets or internal hostnames; `docs/product/*.md` = end-user docs.
- Hangar (org `Infuser-s`) reads only CATALOG and `docs/product/*.md` today; BACKLOG/SECURITY follow the format but aren't read yet. Push, then run a sync.
- All project docs (backlogs, security, plans, designs, runbooks, handoffs) live under `docs/`, never the repo root; the root keeps only `README.md`, `CLAUDE.md`, `CHANGELOG.md` and tool-generated files (e.g. `HELP.md`).
- Put each change in the repo that owns it (shared code → `infusers-library`, authn/authz → `infusers-auth`, UI → `infusers-web-app-stage`, build/deploy → `infusers-scripts`/`jenkins-shared-lib`, domain → owning service); library changes must be published before consumers use them.
- A change to a feature, route, gate, limit, error or availability isn't done until the support bot content reflects it (Infusers bot `infusers-api` `support-kb`, Hangar bot `docs/product`, WealthLens docs); say which you updated, or that none was affected.
<!-- infusers-meta:end -->
