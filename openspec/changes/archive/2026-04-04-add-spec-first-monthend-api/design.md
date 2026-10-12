## Context

The monthend hexagon already exposes a complete inbound use-case surface for worklists, status overview, task completion, clarification lifecycle actions, self-service preparation, and month-end generation. Those use cases are currently exercised through direct tests such as [MonthEndIT](/Users/olivertod/dev/repos/mega-backend/src/test/java/com/gepardec/mega/hexagon/monthend/application/MonthEndIT.java), but there is no dedicated REST API contract for them yet.

The repository already has two relevant constraints:

- REST APIs in the legacy backend are implemented as Quarkus resources with coarse-grained role checks at the HTTP boundary.
- The new `com.gepardec.mega.hexagon` backend is expected to follow DDD + hexagonal rules, which means the REST layer should be a driver adapter and authentication concerns should stay outside the application core.

The main design flaw to fix while adding the API is in self-service preparation: [PrematureMonthEndPreparationService.java](/Users/olivertod/dev/repos/mega-backend/src/main/java/com/gepardec/mega/hexagon/monthend/application/PrematureMonthEndPreparationService.java#L23) resolves the authenticated actor from inside the application service instead of receiving the actor from the driver boundary.

## Goals / Non-Goals

**Goals:**

- Define a single canonical OpenAPI contract for monthend REST endpoints in one file.
- Generate Java API interfaces and HTTP models from that contract during the Maven build.
- Implement thin REST adapters that delegate to the existing monthend inbound use cases.
- Expose all existing monthend use cases through role-secured REST endpoints.
- Move authenticated actor resolution to the REST adapter boundary and pass actor identity into the application core.
- Reuse existing domain behavior and avoid introducing a CQRS mediator layer that does not add business value.

**Non-Goals:**

- Changing monthend domain rules, aggregates, or persistence behavior.
- Replacing the legacy annotation-driven OpenAPI setup for the rest of the service.
- Introducing a command bus, query bus, or separate read store.
- Redesigning the global authentication or authorization mechanism of the whole application.
- Changing database schema or Liquibase changelogs.

## Decisions

### Decision: Use one canonical monthend OpenAPI document and generate server-side interfaces from it

The change will add one monthend-specific OpenAPI YAML file as the canonical HTTP contract. The file will describe the employee, project-lead, shared, and ops monthend endpoints in a single spec, matching the product decision to keep internal ops endpoints in the same contract for now.

The Maven build will add `openapi-generator-maven-plugin` in `generate-sources`, analogous to the existing CXF generation step in [pom.xml](/Users/olivertod/dev/repos/mega-backend/pom.xml#L302). The generator will output Java sources into `target/generated-sources/openapi` and generate:

- JAX-RS server interfaces grouped by tag
- request and response models used by the monthend REST adapter layer

The generator configuration will favor interface-only server generation with Jakarta support so Quarkus resources can implement the generated interfaces directly.

Proposed source and output layout:

```text
src/main/openapi/monthend.openapi.yaml
target/generated-sources/openapi
```

Proposed generated package layout:

```text
com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.api
com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model
```

Alternatives considered:

- Continue annotation-first OpenAPI for monthend: rejected because the user explicitly wants spec-first as the source of truth.
- Split public and internal contracts into separate YAML files: rejected for now because the desired product contract is a single spec.
- Generate full server stubs: rejected because the project already has clear resource and DI patterns; only interfaces and models are needed.

### Decision: Keep the REST layer as a thin driver adapter and do not add a CQRS mediator layer

The monthend core already has a clean application boundary through inbound ports such as worklist queries, status overview queries, task completion, clarification commands, preparation, and generation. The useful part of CQRS is already present in the use-case surface: query use cases return read models, and command use cases mutate aggregates.

The REST implementation will therefore be:

```text
generated OpenAPI interface
  -> handwritten Quarkus REST adapter
  -> existing inbound use case
  -> domain / repositories
```

No additional command bus or query bus will be introduced between the resource adapter and the use cases.

Alternatives considered:

- Add a CQRS dispatcher layer between generated resources and use cases: rejected because it adds indirection without solving a concrete problem in this domain.
- Bypass use cases and call repositories directly from resources: rejected because it breaks the hexagonal boundary.

### Decision: Use four REST adapter groups that match the API contract

The generated API will be tagged and implemented in four groups:

- `MonthEndEmployeeApi`
- `MonthEndProjectLeadApi`
- `MonthEndSharedApi`
- `MonthEndOpsApi`

The handwritten implementations will live in the monthend inbound adapter layer, for example:

```text
com.gepardec.mega.hexagon.monthend.adapter.inbound.rest
```

Proposed endpoint surface:

- `GET /monthend/employee/worklist`
- `POST /monthend/employee/preparations`
- `POST /monthend/employee/clarifications`
- `GET /monthend/project-lead/worklist`
- `POST /monthend/project-lead/clarifications`
- `GET /monthend/status-overview`
- `POST /monthend/tasks/{taskId}/complete`
- `PUT /monthend/clarifications/{clarificationId}/text`
- `POST /monthend/clarifications/{clarificationId}/resolve`
- `POST /monthend/ops/generation`

Clarification creation is intentionally split into employee and project-lead endpoints even though the shared domain use case is the same. This avoids an awkward polymorphic request body, keeps the role boundaries explicit, and lets the adapter set `creatorSide` deterministically.

Alternatives considered:

- One shared clarification creation endpoint with `creatorSide` in the request: rejected because it complicates the OpenAPI schema and pushes role interpretation into the HTTP contract.
- Separate status overview endpoints for employee and project lead: rejected because the current use case is actor-centric and does not need duplication.

### Decision: Resolve actor identity in the REST adapter and pass `UserId` inward

Monthend REST adapters will resolve the current caller from authentication and translate that into the hexagon `UserId` before invoking the use case. The existing application-level auth resolution in self-service preparation will be removed.

Concretely:

- the current adapter-facing actor resolver will live in the inbound REST adapter layer
- it will read the authenticated email from the request security context or existing JWT claims access
- it will resolve the hexagon `UserId` through the existing hexagon user repository
- REST adapters will pass that `UserId` into use cases that act on behalf of the caller

This requires changing the self-service preparation inbound port from:

```text
prepare(YearMonth month, ProjectId projectId, String clarificationText)
```

to an actor-explicit form such as:

```text
prepare(YearMonth month, ProjectId projectId, UserId actorId, String clarificationText)
```

The application service will then use the supplied actor instead of depending on `CurrentMonthEndActorResolver`.

Other monthend command use cases already accept `actorId`, so they naturally fit the adapter-driven boundary.

Alternatives considered:

- Keep auth lookup inside the application service: rejected because it leaks transport concerns into the hexagon core.
- Pass raw email through the use-case boundary: rejected because the application core already uses `UserId` as its actor identity.

### Decision: Reuse existing coarse-grained HTTP security at the resource boundary and keep fine-grained checks in the core

Monthend REST adapters will follow the established resource security style already used by legacy resources such as [WorkerResourceImpl.java](/Users/olivertod/dev/repos/mega-backend/src/main/java/com/gepardec/mega/rest/impl/WorkerResourceImpl.java#L51) and [ProjectManagementResourceImpl.java](/Users/olivertod/dev/repos/mega-backend/src/main/java/com/gepardec/mega/rest/impl/ProjectManagementResourceImpl.java#L55):

- employee resources: authenticated + employee role
- project-lead resources: authenticated + project-lead role
- shared resources: authenticated + employee or project-lead role
- ops resource: internal sync or cron role, mirroring [SyncResourceImpl.java](/Users/olivertod/dev/repos/mega-backend/src/main/java/com/gepardec/mega/rest/impl/SyncResourceImpl.java#L22)

Fine-grained authorization remains in the monthend domain and application layer:

- task eligibility is enforced by `MonthEndTask`
- clarification edit and resolve permissions are enforced by `MonthEndClarification`
- employee self-service preparation remains constrained by project context resolution and subject-employee checks

This keeps the HTTP layer responsible for coarse access and the core responsible for business eligibility.

Alternatives considered:

- Build a new hexagon-specific authorization mechanism for monthend only: rejected because it would duplicate the application’s existing request authorization pattern.
- Rely on HTTP role checks alone: rejected because the business rules are actor- and aggregate-specific.

### Decision: Keep the HTTP contract simple and map transport types explicitly

The OpenAPI contract will model:

- month values as strings in `yyyy-MM` format
- task and clarification IDs as UUIDs
- generated response DTOs for worklists, overview entries, task state, clarification state, preparation result, and generation result

The REST adapter layer will convert transport-specific values into domain types (`YearMonth`, `ProjectId`, `UserId`, `MonthEndTaskId`, `MonthEndClarificationId`) before invoking the core. Mapping between domain/read models and generated DTOs will use MapStruct in the REST adapter package, following the repository rule that HTTP mapping is adapter-local.

This keeps the OpenAPI document portable and avoids depending on custom generator type mappings for `YearMonth`.

Alternatives considered:

- Custom generator type mappings for `YearMonth`: rejected for now because a plain string contract is simpler and more robust.
- Manual mapping inside resources: rejected because the repository standard is to use MapStruct for HTTP mapping.

## Risks / Trade-offs

- [Generated code increases build complexity] → Keep generation isolated to `generate-sources`, target a dedicated package, and never hand-edit generated files.
- [Canonical monthend OpenAPI may diverge from the runtime `/q/openapi` output for the rest of the service] → Treat the monthend YAML as the source of truth for this feature and leave global OpenAPI unification for a later change.
- [Security at the REST boundary still relies on the existing application authorization mechanism, which uses legacy-side role constructs] → Limit that coupling to the adapter edge and keep all monthend business authorization in the hexagon core.
- [Changing the self-service preparation port signature touches application tests and adapters] → Make the signature change early, update affected tests together, and remove the old resolver dependency in one slice.
- [Separate employee and lead clarification creation endpoints create more surface area] → Accept the extra endpoints because they produce a clearer contract and simpler generated request models than a shared polymorphic command.

## Migration Plan

This is an additive API change with no persistence migration.

1. Add the canonical monthend OpenAPI file and Maven generation step.
2. Generate monthend API interfaces and models into `target/generated-sources/openapi`.
3. Introduce handwritten monthend REST adapters and MapStruct HTTP mappers.
4. Change self-service preparation to accept `actorId` from the adapter boundary and remove the application-level auth lookup dependency.
5. Add REST integration tests covering employee, lead, shared, and ops endpoint groups.
6. Deploy the additive endpoints.

Rollback strategy:

- revert the monthend REST adapter classes
- remove the generator configuration and generated source references
- restore the previous self-service preparation signature if the rollout is abandoned before completion

## Open Questions

None blocking for implementation.

The only follow-up question worth tracking beyond this change is whether the canonical monthend OpenAPI document should later be merged into the service-wide runtime OpenAPI output.
