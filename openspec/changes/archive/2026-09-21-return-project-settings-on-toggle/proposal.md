# Proposal

## Why

`PUT /projects/{projectId}/leistungsnachweis-enabled` answers `204 No Content`, so a client learns nothing about the stored result and has to assume that what it sent is what was saved. That assumption holds today only because every rule violation is an explicit `400`. The moment a server-side rule adjusts the value instead of rejecting it — the kind of rule this bounded context already has, where a project turning non-billable forces the flag off — a client would keep showing a value the system does not hold, until the next load.

It is also the only write endpoint in this API that answers with nothing while mutating a single addressable thing. `PUT /monthend/clarifications/{clarificationId}/text`, `POST /monthend/clarifications/{clarificationId}/resolve` and `POST /monthend/tasks/{taskId}/complete` all return the mutated representation; the toggle is the outlier. The contract and the consuming page are still unreleased on this feature branch, so aligning it now costs nothing.

## What Changes

- **BREAKING (unreleased):** `PUT /projects/{projectId}/leistungsnachweis-enabled` answers `200 OK` with the updated `ProjectSettings` entry instead of `204 No Content`. The response body is the same shape a single entry of `GET /projects/settings` has, so a client can render the stored result directly.
- Error responses are unchanged: `400` for a missing `enabled` value or for enabling Leistungsnachweis on a non-billable project, `403` for a caller who does not lead the project, `404` for an unknown project.
- The read endpoint, the domain rules around billability, and month-end generation are untouched.

## Capabilities

### New Capabilities

_(none — this modifies an existing capability)_

### Modified Capabilities

- `project-rest-api`: the toggle endpoint's success response becomes `200 OK` carrying the updated settings entry, instead of an unspecified success response with no body.

## Impact

- **REST contract**: `paths/projects.yaml` — the `204` success response becomes `200` with a `ProjectSettings` body. No schema changes; `ProjectSettings` and `LeistungsnachweisToggleRequest` stay as they are.
- **Generated API**: the `ProjectApi` interface is regenerated. It already returns a JAX-RS `Response`, so the Java signature does not change.
- **Project BC**: `SetLeistungsnachweisEnabledUseCase` returns the updated `Project` instead of `void`; `SetLeistungsnachweisEnabledService` returns the aggregate it already saves; `ProjectResource` maps it with the existing `ProjectRestMapper.toDto` and answers `200`.
- **Tests**: the REST test asserts `200` and the response body; the service test asserts the returned aggregate.
- **Reviewed and unaffected**: `project-aggregate` (the billability rule and its transitions are unchanged), `monthend-task-generation` (generation still reads the persisted flag), `shared-user-project-refs` (the response reuses the shared project reference, whose shape does not change).
- **Frontend follow-up** in `mega-frontend-v2`: pull the contract, regenerate the client, and patch the row from the response instead of from the request value. The in-flight `project-settings-page` change there covers the page.
