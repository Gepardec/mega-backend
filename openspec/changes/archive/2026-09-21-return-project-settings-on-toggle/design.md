# Design

## Context

See proposal.md (Why) for motivation. The pieces this change touches already exist:

- `SetLeistungsnachweisEnabledService` loads the project, checks lead membership, applies `withLeistungsnachweisEnabled` and saves the result. It already holds the updated aggregate in a local variable and logs the transition; it just does not hand it back.
- `SetLeistungsnachweisEnabledUseCase.setLeistungsnachweisEnabled` returns `void`.
- `ProjectRestMapper.toDto(Project)` maps an aggregate to `ProjectSettingsDto`, composing the `project` reference through `SharedRefRestMapper`. `getProjectSettings` already uses it for the list.
- `ProjectResource` implements the generated `ProjectApi`, whose methods return a JAX-RS `Response`, so the declared response body in the contract does not change any Java signature.

## Goals / Non-Goals

**Goals:**

- Let a caller read the persisted result of the write without a second request.
- Keep the write's authorization, validation and error responses exactly as they are.

**Non-Goals:**

- Changing `GET /projects/settings`, the domain rules around billability, or month-end generation.
- Introducing optimistic concurrency (an `ETag` or a version field). Nothing in this context needs it yet, and it would change the request side too.
- A general review of the other endpoints that answer `204`. `DELETE /monthend/clarifications/{id}` returning no body is idiomatic, and the premature-generation trigger has no single representation to return.

## Decisions

### D1: Return the `ProjectSettings` entry, not a bare flag

The body is the same shape `GET /projects/settings` returns for one entry, so a client that renders the list can apply the response to a row without a second mapping. `ProjectSettings` also stays the single place the project's read shape is defined.

*Alternatives considered:*

- **Return only `{ leistungsnachweisEnabled }`.** Smaller, but it introduces a second response shape for the same concept, and a caller holding a list row would still have to merge it by hand.
- **Return the full `Project` aggregate shape (with `zepId` and `billable`).** Rejected: `refine-project-settings` deliberately removed those fields from this API's surface.

### D2: The use case returns the updated aggregate

`setLeistungsnachweisEnabled` returns the `Project` it saved. The service already has it, so nothing extra is loaded, and the REST adapter maps the same aggregate that was persisted rather than re-reading it.

*Alternatives considered:*

- **Keep the use case `void` and have the resource re-read through `GetProjectSettingsUseCase`.** Rejected: a second read for data just written, and a read use case scoped to "the projects I lead" is the wrong port for fetching one project.
- **Return a dedicated application-level result type.** Rejected as premature; the aggregate is what the caller needs, and every other use case in this context returns domain types.

### D3: Contract first, then the adapter

`paths/projects.yaml` changes the `204` success response to `200` with a `ProjectSettings` body; the build regenerates `ProjectApi`. Because the generated methods return `Response`, the compiler will not point at the places that still answer `noContent()` — the REST test asserting the status is what catches it, so it is updated in the same step.

## Risks / Trade-offs

- **[A client written against `204` ignores the body]** → Harmless: the endpoint is unreleased, and an ignored body changes nothing for the caller.
- **[The response reveals the project's name and ZEP link to a caller who only sent a flag]** → Not a new exposure: only a lead of that project reaches this endpoint, and `GET /projects/settings` already returns those fields to the same caller.
- **[The frontend keeps patching its row from the request value]** → It would still be correct today, since violations are rejected rather than coerced. The follow-up switches it to the response so it stays correct if a coercing rule is ever added.

## Migration Plan

1. Ship the backend change. No database or configuration change is involved.
2. Ship the frontend follow-up, which regenerates its client and patches the row from the response.
3. Rollback: reverting is safe. A client that reads the body simply stops receiving one; nothing persists differently.
