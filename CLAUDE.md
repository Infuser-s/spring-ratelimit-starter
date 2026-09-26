# Instructions

Develop industry-standard software across every layer of this codebase — UI, database, backend, API, and every other layer of the stack — following each layer's established conventions rather than introducing one-off patterns.

Perform a security review of your changes where applicable (e.g. auth, input handling, secrets, dependencies, access control) before considering a change complete.

## Repo ownership & where changes belong

We own **every repo under `~/git/`** — they all belong to the Infusers platform (see `infusers-scripts/docs/PROJECTS.md`).
Never treat another repo as off-limits or "out of scope", and never work around a gap with a local one-off. Put each
change in the repo it belongs to:

- **Common/shared code** (exception handling, tenant/entitlement infra, utilities, shared DTOs) → `infusers-library`.
- **Authn/authz, users, orgs, entitlement keys, JWT claims** → `infusers-auth`.
- **UI, routes, entitlement guards/labels** → `infusers-web-app-stage`.
- **Build/deploy/CI** → `infusers-scripts` and `jenkins-shared-lib`.
- **Domain logic** → the service that owns the domain (e.g. `infusers-commerce` for quotation/stock/payment).

When a task spans repos, plan and make the change in each one and keep their docs in sync in the same effort.
`infusers-library` changes must be published to the artifact registry before consumers can use them — flag that
sequencing rather than duplicating the code in the consumer.

## Support bot content must stay current

The AI support bots answer only from curated content, so stale content means wrong or "not covered" answers. A change isn't done until that content reflects it — in the same diff/effort — for any new, changed or removed feature, screen/route, flow, entitlement/plan gate, limit, error message, or platform/environment availability:

- **Infusers bot** (web app + mobile): KB is `infusers-api/src/main/resources/support-kb/*.json` — `feature-matrix.json` (authoritative per-platform/env availability), `faqs.json`, `about.json` (`whatsNew`), topic files (`errors`, `rate-limits`, `troubleshooting`, `ai-chat`, …), and the repo's own `<repo-name>.json` (add one if this Infusers platform repo has none; keep it accurate when purpose/setup/how-to changes). Use `"audience": "admin"` for admin-only content; never put secrets, credentials or internal hostnames in any KB file (it goes into an LLM prompt shown to users). The KB loads once at startup, so `infusers-api` must be rebuilt and deployed for changes to take effect — flag that sequencing. Also keep the static fallback suggestion chips (`getStaticFallbackSuggestions` in `infusers-web-app-stage`'s `ai-support-bot` and `infusers-mobile`'s `SupportBotModal.tsx`) in line with the current screens.
- **Hangar bot**: each repo's flat `docs/product/*.md` and `docs/CATALOG.md`, pulled from GitHub by Hangar sync — push them, then run a sync.
- **WealthLens bot**: `wealth-lens/docs/USER_GUIDE.md`, `product/PRODUCT_SPEC.md`, `security/CONFIGURATION.md`, `security/SECURITY.md`, and the route-based fallback answers in `backend/app/services/support_service.py`.

Make the edit in the repo that owns the content (per the ownership rules). When finishing, say which support content you updated, or that none was affected.
