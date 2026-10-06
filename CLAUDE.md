# Instructions

Build every layer (UI, DB, backend, API, …) to industry standard, following its established conventions; no one-off patterns.

Security-review your changes where applicable (e.g. auth, input handling, secrets, deps, access control) before calling them done.

## Repo ownership & where changes belong

All `~/git/` repos are ours (Infusers platform; `infusers-scripts/docs/PROJECTS.md`). Never treat one as off-limits/"out of scope" or work around a gap with a local one-off; put each change in the repo it belongs to:

- Shared code (exception handling, tenant/entitlement infra, utilities, shared DTOs) → `infusers-library`
- Authn/authz, users, orgs, entitlement keys, JWT claims → `infusers-auth`
- UI, routes, entitlement guards/labels → `infusers-web-app-stage`
- Build/deploy/CI → `infusers-scripts`, `jenkins-shared-lib`
- Domain logic → owning service (e.g. `infusers-commerce`: quotation/stock/payment)

Cross-repo: plan and make the change in each repo and keep their docs in sync, in one effort. `infusers-library` changes must be published to the artifact registry before consumers can use them. Flag that sequencing; don't duplicate the code in consumers.

## Support bot content must stay current

AI support bots answer only from curated content (stale → wrong/"not covered" answers). A change isn't done until that content reflects it, in the same diff/effort and in the owning repo. This covers any new/changed/removed feature, screen/route, flow, entitlement/plan gate, limit, error message, or platform/env availability:

- Infusers bot (web app + mobile): `infusers-api/src/main/resources/support-kb/*.json`. Files: `feature-matrix.json` (authoritative per-platform/env availability), `faqs.json`, `about.json` (`whatsNew`), topic files (`errors`, `rate-limits`, `troubleshooting`, `ai-chat`, …), and the changed repo's own `<repo-name>.json` (web-app-stage's is `infusers-web-app.json`; add one if missing; keep purpose/setup/how-to current). Admin-only content: `"audience": "admin"`. No secrets/credentials/internal hostnames in KB files (they feed a user-facing LLM prompt). The KB loads once at startup, so flag that `infusers-api` must be rebuilt and deployed for changes to take effect. Keep the static fallback chips (`getStaticFallbackSuggestions` in `infusers-web-app-stage`'s `ai-support-bot` and `infusers-mobile/features/support/components/SupportBotModal.tsx`) matching current screens.
- Hangar bot: each repo's flat `docs/product/*.md` and docs/CATALOG.md. Hangar sync pulls them from GitHub, so push, then run a sync.
- WealthLens bot: `wealth-lens/docs/USER_GUIDE.md`, `wealth-lens/docs/product/PRODUCT_SPEC.md`, `wealth-lens/docs/security/CONFIGURATION.md`, `wealth-lens/docs/security/SECURITY.md`, and the route-based fallback answers in `wealth-lens/backend/app/services/support_service.py`.

When finishing, say which support content you updated, or that none was affected.

<!-- infusers-meta:begin -->
## Documentation & shared rules (managed in infusers-meta)

Shared rules: [infusers-meta/docs/agent-rules](https://github.com/Infuser-s/infusers-meta/tree/master/docs/agent-rules) (`ownership.md`, `support-bot-content.md`, `documentation.md`, `test-and-monitor-strategy.md`). Read them from a sibling checkout if present, otherwise from that link; this block is the self-contained summary and works on any machine or user. Doc table formats: [infusers-hangar DOC_FORMAT.md](https://github.com/Infuser-s/infusers-hangar/blob/master/docs/engineering/DOC_FORMAT.md).

- Docs live in `docs/` and are updated in the same change as the code. `docs/CATALOG.md` (pipe table `Feature | Status | Summary | Doc`) = what's shipped; `docs/BACKLOG.md` (`ID | Title | Priority | Status | Doc | Type`) = open work only, row removed in the change that finishes it; `docs/SECURITY.md` (`ID | Title | Severity | Status | Doc`) = open findings only, no secrets or internal hostnames; `docs/product/*.md` = end-user docs.
- Hangar (org `Infuser-s`) reads only CATALOG and `docs/product/*.md` today; BACKLOG/SECURITY follow the format but aren't read yet. Push, then run a sync.
- All project docs (backlogs, security, plans, designs, runbooks, handoffs) live under `docs/`, never the repo root; the root keeps only `README.md`, `CLAUDE.md`, `CHANGELOG.md` and tool-generated files (e.g. `HELP.md`).
- Put each change in the repo that owns it (shared code → `infusers-library`, authn/authz → `infusers-auth`, UI → `infusers-web-app-stage`, build/deploy → `infusers-scripts`/`jenkins-shared-lib`, domain → owning service); library changes must be published before consumers use them.
- Infra docs routing: host/network/Proxmox facts and per-host scripts → `infusers-scripts`; Compose stacks and their runbooks → `infusers-home-lab`; cross-repo policy → `infusers-meta`. **Home-lab capacity is frozen (2026-10-06, until new hardware)**: no more CPU/RAM or new VMs; optimize existing ones, keep new services net-neutral ([rules](https://github.com/Infuser-s/infusers-meta/blob/master/docs/agent-rules/infrastructure-capacity.md)).
- A change to a feature, route, gate, limit, error or availability isn't done until the support bot content reflects it (Infusers bot `infusers-api` `support-kb`, Hangar bot `docs/product`, WealthLens docs); say which you updated, or that none was affected.
<!-- infusers-meta:end -->
