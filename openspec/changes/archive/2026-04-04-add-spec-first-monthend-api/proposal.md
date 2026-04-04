## Why

The monthend domain is now feature-complete in the hexagonal core, but its use cases are not yet exposed through a dedicated REST API. We need a spec-first API contract now so frontend and backend development can align on stable endpoints, models, and role-based access before implementation details spread ad hoc through handwritten resources.

## What Changes

- Add a spec-first monthend REST API defined in a single OpenAPI document that covers all current monthend use cases.
- Expose employee worklist, project-lead worklist, shared status overview, self-service preparation, task completion, clarification lifecycle actions, and internal generation through REST endpoints.
- Define role-based access in the API contract so employee, project-lead, and internal ops endpoints are protected consistently.
- Generate Java API interfaces and models from the OpenAPI definition and implement thin REST adapters that delegate to existing monthend inbound use cases.
- Fix the current auth-boundary design flaw by moving authenticated actor resolution to the REST adapter boundary instead of resolving it from inside the application service.

## Capabilities

### New Capabilities
- `monthend-rest-api`: Provides a role-secured, spec-first REST API for all existing monthend use cases, including generated request and response models.

### Modified Capabilities
None.

## Impact

- Adds a new public/internal monthend API contract and generated Java sources derived from OpenAPI.
- Affects the monthend hexagon inbound boundary, REST adapter layer, Maven build configuration, and security annotations.
- Requires new REST integration tests for employee, project-lead, shared, and ops flows.
- Refactors self-service preparation so actor identity is supplied by the driver adapter instead of being resolved inside the application service.
