# Instructions

Build every layer (UI, DB, backend, API, …) to industry standard, following its established conventions; no one-off patterns.

Security-review your changes where applicable (e.g. auth, input handling, secrets, deps, access control) before calling them done.

## Documentation & shared rules (managed in infusers-meta)

Shared rules live in `infusers-meta/docs/agent-rules/` (`ownership.md`, `support-bot-content.md`, `documentation.md`); read them rather than recalling. Inline essentials:

- Docs in `docs/`, updated in the same change; table formats per `infusers-hangar/docs/engineering/DOC_FORMAT.md`.
- `docs/CATALOG.md` = what's shipped; `docs/BACKLOG.md` = open work only (row removed in the change that finishes it); `docs/SECURITY.md` = open findings only; `docs/product/*.md` = end-user docs. Hangar syncs these from GitHub: push, then run a sync.
- Put each change in the repo that owns it (shared code → `infusers-library`, authn/authz → `infusers-auth`, UI → `infusers-web-app-stage`, build/deploy → `infusers-scripts`/`jenkins-shared-lib`, domain → owning service); library changes must be published before consumers use them.
- A change to a feature, route, gate, limit, error or availability isn't done until the support bot content reflects it (Infusers bot `infusers-api` `support-kb`, Hangar bot `docs/product`, WealthLens docs); say which you updated, or that none was affected.
