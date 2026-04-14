## ADDED Requirements

### Requirement: UserRef is the canonical cross-module user reference
The shared kernel SHALL provide a `UserRef` record in `shared/domain/model/` with fields `UserId id`, `FullName fullName`, and `ZepUsername zepUsername`. `UserRef` SHALL be the only type used to reference a user from outside the `user` module's domain layer. No module outside `user` SHALL declare its own projection or snapshot type for basic user identity data.

#### Scenario: Monthend module references a user via UserRef
- **WHEN** a `MonthEndWorklistItem` or `MonthEndStatusOverviewItem` carries a reference to a subject employee
- **THEN** the reference type is `UserRef` from `shared/domain/model/`
- **THEN** no `MonthEndEmployee` or `MonthEndUserSnapshot` type exists in the monthend module

#### Scenario: Worktime module references a user via UserRef
- **WHEN** a `WorkTimeEntry` carries a reference to an employee
- **THEN** the reference type is `UserRef` from `shared/domain/model/`
- **THEN** no `WorkTimeEmployee` or `WorkTimeUserSnapshot` type exists in the worktime module

### Requirement: ProjectRef is the canonical cross-module project reference
The shared kernel SHALL provide a `ProjectRef` record in `shared/domain/model/` with fields `ProjectId id`, `int zepId`, and `String name`. `ProjectRef` SHALL be the only type used to reference a project from outside the `project` module's domain layer when only identity and display data is needed. No module outside `project` SHALL declare its own minimal project reference or snapshot type carrying only `{ id, name }` or `{ id, zepId, name }`.

#### Scenario: Monthend minimal project reference uses ProjectRef
- **WHEN** a `MonthEndWorklistItem` or `MonthEndStatusOverviewItem` carries a project reference
- **THEN** the reference type is `ProjectRef` from `shared/domain/model/`
- **THEN** no `MonthEndProject` type exists in the monthend module

#### Scenario: Worktime module references a project via ProjectRef
- **WHEN** a `WorkTimeEntry` carries a reference to a project
- **THEN** the reference type is `ProjectRef` from `shared/domain/model/`
- **THEN** no `WorkTimeProject` or `WorkTimeProjectSnapshot` type exists in the worktime module

### Requirement: Ports returning users or projects across module boundaries expose only active records
Any outbound port in the `monthend` or `worktime` modules that returns user or project data SHALL accept a `YearMonth` parameter and return only records active during that month. Activeness filtering SHALL be enforced inside the adapter implementation — not in the application service after fetching all records.

#### Scenario: User snapshot port returns only active users for the given month
- **WHEN** `findActiveIn(YearMonth month)` is called on the user snapshot port
- **THEN** only users with an employment period covering the given month are returned
- **THEN** inactive users are excluded before the result reaches the application service

#### Scenario: Project snapshot port returns only active projects for the given month
- **WHEN** `findActiveIn(YearMonth month)` is called on the project snapshot port
- **THEN** only projects whose date range overlaps the given month are returned
- **THEN** inactive projects are excluded before the result reaches the application service

#### Scenario: Application service does not filter activeness
- **WHEN** `GenerateMonthEndTasksService.generate(YearMonth)` runs
- **THEN** the service does not call `.filter(u -> u.isActiveIn(month))` on the user list
- **THEN** the service does not call `.filter(p -> p.isActiveIn(month))` on the project list
