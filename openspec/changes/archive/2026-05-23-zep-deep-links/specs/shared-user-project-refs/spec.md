## MODIFIED Requirements

### Requirement: ProjectRef is the canonical cross-module project reference
The shared kernel SHALL provide a `ProjectRef` record in `shared/domain/model/` with fields `ProjectId id`, `int zepId`, and `String name`. `ProjectRef` SHALL be the only type used to reference a project from outside the `project` module's domain layer when only identity and display data is needed. No module outside `project` SHALL declare its own minimal project reference or snapshot type carrying only `{ id, name }` or `{ id, zepId, name }`. The REST representation of `ProjectRef` SHALL include a `zepUrl` field containing a pre-assembled URL pointing to the project's page in the ZEP web interface. `zepUrl` SHALL be assembled server-side by the REST adapter and SHALL be `null` when the ZEP identifier is unavailable.

#### Scenario: Monthend REST adapter enriches task entries with ProjectRef
- **WHEN** the monthend REST adapter maps a `MonthEndTask` to a status overview entry
- **THEN** the project reference in the entry is of type `ProjectRef` from `shared/domain/model/`
- **THEN** no `MonthEndProject` type exists in the monthend module

#### Scenario: Project reference DTO includes a pre-assembled ZEP URL
- **WHEN** a `ProjectRef` is mapped to its REST DTO
- **THEN** the DTO includes a `zepUrl` field containing the full URL to the project's page in ZEP
- **THEN** the URL is assembled from the configured ZEP origin and the project's `zepId`

#### Scenario: Worktime module references a project via ProjectRef
- **WHEN** a `WorkTimeEntry` carries a reference to a project
- **THEN** the reference type is `ProjectRef` from `shared/domain/model/`
- **THEN** no `WorkTimeProject` or `WorkTimeProjectSnapshot` type exists in the worktime module

### Requirement: UserRef is the canonical cross-module user reference
The shared kernel SHALL provide a `UserRef` record in `shared/domain/model/` with fields `UserId id`, `FullName fullName`, and `ZepUsername zepUsername`. `UserRef` SHALL be the only type used to reference a user from outside the `user` module's domain layer. No module outside `user` SHALL declare its own projection or snapshot type for basic user identity data. The REST representation of `UserRef` SHALL include a `zepUrl` field containing a pre-assembled URL pointing to the employee's page in the ZEP web interface. `zepUrl` SHALL be assembled server-side by the REST adapter and SHALL be `null` when `zepUsername` is unavailable.

#### Scenario: Monthend REST adapter enriches task entries with UserRef
- **WHEN** the monthend REST adapter maps a `MonthEndTask` to a status overview entry
- **THEN** the subject employee reference in the entry is of type `UserRef` from `shared/domain/model/`
- **THEN** no `MonthEndEmployee` or `MonthEndUserSnapshot` type exists in the monthend module

#### Scenario: User reference DTO includes a pre-assembled ZEP URL
- **WHEN** a `UserRef` is mapped to its REST DTO
- **THEN** the DTO includes a `zepUrl` field containing the full URL to the employee's page in ZEP
- **THEN** the URL is assembled from the configured ZEP origin and the employee's `zepUsername`

#### Scenario: User reference DTO zepUrl is null when zepUsername is absent
- **WHEN** a `UserRef` with a null `zepUsername` is mapped to its REST DTO
- **THEN** the `zepUrl` field in the DTO is `null`

#### Scenario: Worktime module references a user via UserRef
- **WHEN** a `WorkTimeEntry` carries a reference to an employee
- **THEN** the reference type is `UserRef` from `shared/domain/model/`
- **THEN** no `WorkTimeEmployee` or `WorkTimeUserSnapshot` type exists in the worktime module
