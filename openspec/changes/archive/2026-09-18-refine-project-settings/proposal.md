# Proposal

## Why

The project-lead Leistungsnachweis settings, introduced by `configurable-leistungsnachweis-generation`, work but are rough in three ways:

- **Duplicated project shape.** The settings list returns its own flat project shape, which nearly duplicates the shared project reference and exposes ZEP's internal id.
- **Too many projects listed.** The list includes projects where the toggle can never have an effect: non-billable projects, and projects that have already ended.
- **Rule in the wrong place.** The rule "only billable projects get a Leistungsnachweis" lives in month-end generation and is repeated in the frontend.

While tracing this, we also found that the shared project reference's `zepUrl` is declared nullable although it can never be null. The worktime endpoints never fill it in at all, which violates the existing `shared-user-project-refs` requirement. The contract and the frontend page are both still unreleased on the feature branch, so reshaping them now is cheap.

## What Changes

- **BREAKING (unreleased):** Replace `GET /projects` with `GET /projects/settings`. Each entry becomes a `ProjectSettings` object: `{ project: ProjectRef, leistungsnachweisEnabled }`.
  - `zepId` and `billable` are no longer returned. The project is referenced through the shared `ProjectRef` shape, which carries a ZEP deep link.
- The settings list returns only projects where the toggle can still affect a future month-end generation run: projects the caller leads that are **billable** and **have not ended before the current month**. Projects starting in the future are included.
- **New domain rule on the project:** a non-billable project always has Leistungsnachweis disabled.
  - A new project starts with the flag equal to its billability.
  - When ZEP sync makes a project non-billable, the flag is forced off.
  - When ZEP sync makes a non-billable project billable again, the flag is reset to on (the opt-out default).
  - Existing stored data is corrected to follow the rule.
- `PUT /projects/{projectId}/leistungsnachweis-enabled` rejects enabling Leistungsnachweis on a non-billable project with `400`. Disabling it on such a project is allowed and has no effect.
- A missing or `null` `enabled` value is rejected with `400` by standard request validation. The redundant hand-written check in the endpoint is removed, and a REST test now covers the case.
- The project reference's `zepUrl` becomes always present and **never null**. The user reference's `zepUrl` stays nullable.
- **Bug fix:** worktime report entries now carry a populated `zepUrl` for both the project and the employee reference. Today these fields are always `null`.
- Internal restructuring, with no behavior change beyond the above:
  - One shared REST mapping of the project and user references, including the ZEP URL, used by the monthend, worktime and project adapters.
  - The combined project settings application service is split into one service per use case.
  - A single-project lookup is added to the project repository.
  - Lead authorization is expressed on the project aggregate.
  - Month-end Leistungsnachweis generation checks only the project flag and the active leads, because the project aggregate now includes billability in the flag.
- A settings change is logged with the actor, the project and the new value.

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `project-rest-api`:
  - The list endpoint moves to `/projects/settings` and returns the composed settings shape.
  - The list is filtered to projects where the toggle is still effective.
  - The toggle endpoint rejects enabling on non-billable projects and rejects a missing `enabled` value.
- `project-aggregate`:
  - `leistungsnachweisEnabled` is now bound to billability: on creation it defaults to the project's billability, and it is forced off while the project is non-billable.
  - The flag reacts to billability changes during ZEP resync.
  - Enabling it on a non-billable project is rejected.
  - Stored data follows the rule.
- `shared-user-project-refs`:
  - The project reference's `zepUrl` is always present and never null.
  - Every REST response that carries project or user references, including the worktime report, fills in their ZEP URLs.

Reviewed and deliberately left unchanged:
- `monthend-task-generation`: observable generation behavior is unchanged. Non-billable projects still never produce a Leistungsnachweis task. Only the internal check gets simpler.
- `project-sync`: the flag's reaction to sync is specified on the aggregate.
- `project-leads-sync`: lead replacement still preserves the flag.
- `worktime-rest-api`: it does not describe reference fields. The worktime fix is specified under `shared-user-project-refs`.
- `monthend-rest-api` and `monthend-status-overview`: their response shapes are unchanged. The project reference `zepUrl` was already filled in there.

## Impact

- **REST contract:**
  - The project paths and schemas change: `ProjectItem` is replaced by `ProjectSettings`, and the read operation is renamed and moved to `/projects/settings`.
  - The shared `ProjectRef` schema's `zepUrl` becomes non-nullable.
  - The generated API interfaces and models change accordingly.
- **Project BC:**
  - `Project` aggregate: invariant, transitions, and new domain methods for lead membership and settings relevance.
  - New domain exception for enabling on a non-billable project.
  - Repository port and adapter: single-project lookup.
  - Two application services replace one; the read use case is renamed.
  - REST resource and mapper.
- **Monthend BC:** the Leistungsnachweis condition in task planning; the REST mapper uses the shared reference mapper.
- **Worktime BC:** the REST mapper uses the shared reference mapper, which fixes the `zepUrl` bug.
- **Shared:** a new shared REST reference mapper that wraps the legacy ZEP configuration.
- **Persistence:** the existing Leistungsnachweis column changelog is edited in place (it has not been applied to any environment) so that stored non-billable projects hold the flag as disabled.
- **Frontend (follow-up, out of scope here):** the `mega-frontend-v2` project settings page switches to the new endpoint and model:
  - drops the ZEP-ID and billable columns
  - links the project name via `zepUrl`
  - disables the toggle only while a save is pending
- **Out of scope:**
  - The `400` body returned by request validation differs from the contract's `ApiError` shape. This affects every endpoint and needs its own change.
  - Composing the monthend project snapshot from the shared project reference.
