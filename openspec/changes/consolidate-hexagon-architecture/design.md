## Context

The hexagonal backend (`com.gepardec.mega.hexagon`) follows a BC-first (module-first) package structure with modules `monthend`, `project`, `user`, `worktime`, and `shared`. An architectural review identified four structural misplacements:

1. **14 inbound port interfaces and 3 supporting sync result records** live in `*.domain.port.inbound` across all four modules — domain packages that should not own application boundary contracts.
2. **`MonthEndTaskPlanningService`** lives in `monthend/application/` but contains only pure task-planning business rules with no I/O or port dependencies.
3. **`SyncScheduler`** lives in `application/schedule/` but is a Quarkus `@Scheduled` component that drives use cases — an inbound adapter.
4. **`UserId`, `Email`, and `ProjectId`** live in module-specific domain packages (`user/domain/model/`, `project/domain/model/`) despite being consumed by every module in the single bounded context.

All four are package-placement issues only. No logic, signatures, or behaviour changes are required.

## Goals / Non-Goals

**Goals:**
- Move inbound port contracts to `*.application.port.inbound/` so the application layer owns its driver port contracts without cluttering the application root.
- Move `MonthEndTaskPlanningService` to `monthend/domain/services/` to reflect its domain service nature.
- Move `SyncScheduler` to `shared/adapter/inbound/` to reflect its inbound adapter nature.
- Move `UserId`, `Email`, and `ProjectId` to `shared/domain/model/` to establish a proper shared kernel.
- Update `ArchitectureTest.java` package path strings to reflect the new inbound port location.
- Update the two affected specs (`hexagon-boundary-conventions`, `project-aggregate`) with delta files.

**Non-Goals:**
- No logic changes to any moved class.
- No changes to outbound ports — driven port interfaces remain in `*.domain.port.outbound/` (domain owns the contract for what it needs from infrastructure).
- No changes to `AuthenticatedActorContext` or presentation mappers (separate concerns).
- No database schema or API contract changes.

## Decisions

### 1. Inbound ports in `application/port/inbound`, outbound ports in `domain/`

Driver ports (inbound `*UseCase` interfaces) and their small boundary-specific contract records define how the outside world drives the application. They are application boundary contracts, not domain concepts. Placing them in `*.application.port.inbound/` keeps the application boundary explicit while leaving the `application/` root focused on orchestration implementations.

Driven ports (outbound `*Port` interfaces such as `UserRepository`) stay in `domain/` because the domain owns the contract — it defines what it needs from infrastructure. Moving them to `application/` would invert the dependency rule.

**Alternative considered:** Place inbound contracts directly in the `application/` root. Rejected: it works technically, but it clutters the application package and blurs the distinction between contracts and implementations.

### 2. `MonthEndTaskPlanningService` is a domain service

The class determines which `MonthEndTaskType` values to create for a project (billability rules, lead requirements, employee task conditions) with no I/O and no port dependencies. This is the textbook definition of a stateless domain service — logic that does not fit naturally in a single entity or value object. The `@ApplicationScoped` CDI annotation is a runtime concern, not an architectural one; domain services can be CDI beans.

**Alternative considered:** Keep in `application/` as a helper service. Rejected: the application layer should contain orchestration logic that coordinates domain objects and ports, not pure business rules.

### 3. `SyncScheduler` is an inbound adapter

A Quarkus `@Scheduled` component is a framework mechanism that drives the application on a timer — the exact definition of an inbound adapter. It belongs in `adapter/inbound/`. Because it coordinates three use cases from different modules (`SyncUsersUseCase`, `SyncProjectsUseCase`, `SyncProjectLeadsUseCase`), `shared/adapter/inbound/` is the correct home.

**Alternative considered:** Keep in `application/schedule/` as an application service. Rejected: the application layer should not contain framework-bound infrastructure components. Schedulers, REST resources, and message consumers are all inbound adapters.

### 4. Shared identity types in `shared/domain/model/`

`UserId`, `Email`, and `ProjectId` have the same meaning in every module of the single bounded context. Keeping them in module-specific domains creates cross-module coupling that correctly belongs in a shared kernel. Within a single BC with modules, the shared kernel is the right home — there is no BC boundary preventing this.

**Alternative considered:** Keep in module domains and have other modules import across. Rejected: this is the exact problem the shared kernel pattern solves. Import direction should flow from `shared/` outward, not between sibling modules.

## Risks / Trade-offs

- **Widespread import churn** → Mitigation: Use IDE refactoring (IntelliJ "Move Class") to update all references atomically. Both `src/main` and `src/test` must be migrated together.
- **ArchUnit test failures during migration** → Mitigation: Update `ArchitectureTest.java` package path strings in the same commit as the moves. Tests will fail to compile until imports are resolved, so this is caught at build time.
- **Missing a reference** → Mitigation: The Java compiler catches all stale imports. The build will not pass until all references are updated.

## Migration Plan

Perform moves using IDE refactoring to ensure all references are updated automatically.

1. Move `UserId`, `Email` from `hexagon.user.domain.model` → `hexagon.shared.domain.model`
2. Move `ProjectId` from `hexagon.project.domain.model` → `hexagon.shared.domain.model`
3. Move each `*UseCase` interface from `<module>.domain.port.inbound` → `<module>.application.port.inbound` and move supporting sync result records there as well
4. Move `MonthEndTaskPlanningService` from `hexagon.monthend.application` → `hexagon.monthend.domain.services`
5. Move `SyncScheduler` from `hexagon.application.schedule` → `hexagon.shared.adapter.inbound`
6. Update `ArchitectureTest.java` — replace package path strings referencing `domain.port.inbound` with `application.port.inbound`
7. Verify full build passes (`mvn clean package`)

Rollback: all changes are reversible by reversing the package moves and import updates. No schema or API changes to undo.
