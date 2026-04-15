## Context

The hexagonal backend has a strict rule: application services orchestrate, domain services encapsulate business logic. A review found four areas where this boundary is violated: a monthend context resolver misplaced in the application package, role assignment policy embedded in two sync services, and worktime hour calculations duplicated across two worktime services.

## Goals / Non-Goals

**Goals:**
- Relocate `ResolveMonthEndEmployeeProjectContextService` to the domain layer
- Extract user role assignment policy into a domain service
- Extract project lead role reconciliation into a domain service and move role mutation onto the `User` aggregate
- Extract worktime hour aggregation into a single domain service, eliminating the duplication

**Non-Goals:**
- No observable behavior changes
- No API, REST, or persistence changes
- No changes to the sync scheduling logic or result types
- No generic sync abstraction (e.g., shared `ChangeType` enum) — each sync service keeps its own private copy

## Decisions

### 1. `ResolveMonthEndEmployeeProjectContextService` moves to domain layer, renamed

The service only depends on domain ports (`MonthEndProjectSnapshotPort`, `MonthEndUserSnapshotPort`, `MonthEndProjectAssignmentPort`) and domain models — it qualifies as a domain service. It is relocated to `monthend.domain.services` and renamed to `MonthEndEmployeeProjectContextService` to follow the naming convention of existing domain services (no `Resolve` prefix).

*Alternative considered*: Keep it in the application layer as a shared application helper. Rejected because the invariants it enforces (employee active, project active, employee assigned) are domain rules, not orchestration.

### 2. `OfficeManagementEmails` value object bridges config to domain; `UserRolePolicyService` is a CDI bean

The role assignment policy needs the OM email list, which comes from a `@ConfigProperty`. Injecting config annotations directly into a domain service would pull a MicroProfile/Quarkus framework dependency into the domain layer.

The solution follows the same pattern as `Clock`: the domain declares what it needs as a pure Java type, and the application layer produces it as a CDI bean.

- `OfficeManagementEmails` is a domain value object (plain Java record) in `user.domain.model`, wrapping a `Set<String>` of normalised email addresses. It exposes a `contains(String email) → boolean` method that handles case-insensitive, trimmed comparison internally.
- An `OfficeManagementEmailsProducer` in `user.application` reads `@ConfigProperty` and produces an `@ApplicationScoped` `OfficeManagementEmails` CDI bean.
- `UserRolePolicyService` is a CDI `@ApplicationScoped` domain service that injects `OfficeManagementEmails`. No framework annotation touches the domain.

*Alternative considered*: Plain Java class constructed by `SyncUsersService`. Rejected because it prevents CDI injection, makes testing harder, and is inconsistent with the other domain services in this change.

### 3. Role mutation moves onto the `User` aggregate

The current `updateLeadRole(user, shouldBeLead)` in `SyncProjectLeadsService` directly manipulates a user's role set via `user.withRoles(...)`. This is an aggregate operation — it belongs on `User`. Two methods are added: `User.grantProjectLeadRole()` and `User.revokeProjectLeadRole()`. Both return a new immutable `User` instance.

### 4. `ProjectLeadRoleReconciliationService` is a CDI domain service

The policy "a user's `PROJECT_LEAD` role must match whether they lead any project" and its implementation (which users need role changes, in which direction) is domain logic. This is extracted to `user.domain.services.ProjectLeadRoleReconciliationService`. It takes all users and the full set of lead user IDs, and returns only the users whose roles needed updating. The application service (`SyncProjectLeadsService`) persists the result.

### 5. `WorkTimeReportAssembler` is a CDI domain service

The assembler encapsulates the domain rule for how raw attendance records translate into work time entries: sum of `billableHours`, sum of `nonBillableHours`, and total hours across all of an employee's projects for the month. It is stateless and has no config dependencies, so a CDI `@ApplicationScoped` domain service is appropriate here.

The assembler exposes two operations:
- `totalHours(List<WorkTimeAttendance>) → double` — sum of all hours in a list of attendances
- `buildEntry(UserRef, ProjectRef, List<WorkTimeAttendance>, double totalMonthHours) → WorkTimeEntry` — assembles a single entry for one employee-project combination

Both `GetEmployeeWorkTimeService` and `GetProjectLeadWorkTimeService` inject it and call these instead of duplicating the private calculation methods.

## Risks / Trade-offs

- Renaming `ResolveMonthEndEmployeeProjectContextService` requires updating all call sites in the application layer; this is a small, contained change.
- `OfficeManagementEmailsProducer` adds a new CDI producer; the produced bean must be `@ApplicationScoped` to avoid re-constructing the set on every injection point.
