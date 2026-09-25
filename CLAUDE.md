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
