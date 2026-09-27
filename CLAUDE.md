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

<!-- agent-coordination:begin v1 -->
## Parallel sessions
Read-only work, subagents (parent claims): don't claim, obey the rest. Can't push claims? Say so, go on; yours = files you changed.
- **List** before first write per repo, after compaction, and before widening/commit/server/deps: `git status -s; git fetch -qp origin '+refs/claims/*:refs/claims/*'; git log --no-walk --glob=claims --format='%S %cr %an@%s'`
- **Claim**: `c=$(git commit-tree $(git mktree </dev/null) -m "$(hostname -s) · <session title> · owns: <paths, ports, DB, deps>") && git push -q origin "$c":refs/claims/<task>` (no quotes/backticks/$/secrets). Rejected: rename. Re-list: last-listed (oldest) overlapping claim wins; loser drops the overlap. Update yours: `push -f`.
- **Others'** changes (dirty/staged outside your claim, even "subagent leftovers"), claims, branches, worktrees: never revert/stash (pull `--no-autostash`)/clean/delete/commit/adopt them or switch/rebase/reset under them without user OK.
- **Overlap** (other claims, any branch/status; unclaimed dirty files): do other parts; `SendMessage` the owner if in `ListAgents`, else ask the user. Pinged? ETA or release.
- **Shared** catalog/backlog/security/changelog: claim/edit only your rows, last; stage only if its `git diff` is all yours, else ask. Next migration/ID number: claim `<kind>-<n>`.
- **Runtime**: check ports (`ss -ltnp`; macOS `lsof -nP -iTCP -sTCP:LISTEN`) before starting servers; restart/migrate/reset/deploy others'/shared things only with owner/user OK.
- **Commit** (if you commit): `git add <your paths> && git commit` (never `-A`/`-a`/`.`/`commit -- <paths>`); `git show --stat HEAD` = yours + hook bumps, else `git reset --soft @~`, ask. Push only if `git log HEAD --not --remotes` is all yours.
- **Compaction summaries** keep: claim names/status, files changed and why, decisions, next steps, user preferences.
- **Close**: done, docs updated, all committed → delete your claims (`git push -q origin :refs/claims/<task>`) and merged branches/worktrees you made. Else re-push `status: review|handoff|blocked`, tell the user.
<!-- agent-coordination:end -->
