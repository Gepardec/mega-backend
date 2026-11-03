## ADDED Requirements

### Requirement: Monthend status overview is available per capacity
The system SHALL provide `GET /monthend/{month}/status-overview` with a required `capacity` query parameter. With `capacity=EMPLOYEE` it SHALL return the tasks and clarifications where the authenticated actor is the subject. With `capacity=CONTROLLING` it SHALL return all tasks and clarifications for projects the authenticated actor leads. Both capacities SHALL return the same response shape.

#### Scenario: Actor retrieves their overview in the employee capacity
- **WHEN** an authenticated actor requests `GET /monthend/{month}/status-overview?capacity=EMPLOYEE`
- **THEN** the API returns tasks where the actor is the subject, including both open and completed tasks
- **THEN** the API returns all clarifications where the actor is the subject for that month, including both open and resolved clarifications

#### Scenario: Project lead retrieves their overview in the controlling capacity
- **WHEN** an authenticated project lead requests `GET /monthend/{month}/status-overview?capacity=CONTROLLING`
- **THEN** the API returns all tasks for projects the lead leads, including both open and completed tasks
- **THEN** the API returns all clarifications for projects the lead leads for that month, including both open and resolved clarifications

#### Scenario: Overview task entries carry display references and the capacity-aware completion flag
- **WHEN** an authenticated actor requests the status overview in either capacity
- **THEN** each overview task entry includes a nested project object containing the project identifier and project name
- **THEN** each overview task entry includes a nullable nested subject employee object containing the employee identifier and full name
- **THEN** each overview task entry includes a `canComplete` field that is `true` only if the actor is eligible to complete that task and the task type is a duty of the requested capacity, and `false` otherwise

#### Scenario: Overview clarification entries carry display references and actor-based capability flags
- **WHEN** an authenticated actor requests the status overview in either capacity
- **THEN** each overview clarification entry includes a nullable nested employee reference object for `subjectEmployee` and a nested employee reference object for `createdBy`
- **THEN** each resolved overview clarification entry includes a nested employee reference object for `resolvedBy` and the resolution fields
- **THEN** each overview clarification entry includes a `canResolve` field set to `true` if the actor is eligible to resolve that clarification and `false` otherwise
- **THEN** each overview clarification entry includes `canEditText` and `canDelete` fields set to `true` if the actor is the creator of that clarification and it is open, `false` otherwise
- **THEN** the clarification capability flags are the same in both capacities

#### Scenario: Project lead reads their own employee overview
- **WHEN** an authenticated project lead requests `GET /monthend/{month}/status-overview?capacity=EMPLOYEE`
- **THEN** the API returns only tasks and clarifications where the lead is the subject employee
- **THEN** the response is identical in shape to the overview in the controlling capacity

#### Scenario: Status overview without a capacity is rejected
- **WHEN** an authenticated actor requests `GET /monthend/{month}/status-overview` without the `capacity` query parameter
- **THEN** the API rejects the request with `400 Bad Request`

#### Scenario: Status overview with an invalid month is rejected
- **WHEN** an authenticated actor requests the status overview with a malformed `month`
- **THEN** the API rejects the request with `400 Bad Request`

### Requirement: Payroll month is available per capacity
The system SHALL provide `GET /monthend/payroll-month` with a required `capacity` query parameter. It SHALL return the payroll month resolved for the authenticated actor by the resolution rule of the requested capacity, as defined in the `payroll-month` capability: the employee rule for `capacity=EMPLOYEE`, the project-lead rule for `capacity=CONTROLLING`.

#### Scenario: Actor retrieves their payroll month in the employee capacity
- **WHEN** an authenticated actor requests `GET /monthend/payroll-month?capacity=EMPLOYEE`
- **THEN** the API returns the payroll month resolved by the employee rule as a `YearMonth` string in `yyyy-MM` format

#### Scenario: Project lead retrieves their payroll month in the controlling capacity
- **WHEN** an authenticated project lead requests `GET /monthend/payroll-month?capacity=CONTROLLING`
- **THEN** the API returns the payroll month resolved by the project-lead rule as a `YearMonth` string in `yyyy-MM` format

#### Scenario: The two capacities may resolve different months for the same actor
- **WHEN** an authenticated project lead requests the payroll month once with `capacity=EMPLOYEE` and once with `capacity=CONTROLLING`
- **THEN** each response reflects the rule of its capacity applied to the authenticated lead
- **THEN** the two responses may differ

#### Scenario: Payroll month without a capacity is rejected
- **WHEN** an authenticated actor requests `GET /monthend/payroll-month` without the `capacity` query parameter
- **THEN** the API rejects the request with `400 Bad Request`

#### Scenario: Unauthenticated caller cannot access the payroll month endpoint
- **WHEN** an unauthenticated caller requests `GET /monthend/payroll-month`
- **THEN** the API rejects the request as unauthorized

## MODIFIED Requirements

### Requirement: Actor-scoped monthend endpoints derive the acting user from authentication
The system SHALL treat the authenticated caller as the acting monthend actor for all actor-scoped monthend REST endpoints. Actor-scoped requests MUST NOT require a caller-supplied actor identifier for the status overview, payroll month, task completion, project-lead bulk task completion, employee bulk task completion, clarification edit, clarification resolve, employee self-service preparation, or clarification deletion.

#### Scenario: Task completion is attributed to the authenticated caller
- **WHEN** an authenticated eligible actor completes a monthend task through the REST API
- **THEN** the completion uses the authenticated actor as the acting user
- **THEN** the request does not require a separate actor identifier

#### Scenario: Bulk task completion is attributed to the authenticated caller
- **WHEN** an authenticated actor completes monthend tasks through either bulk task completion endpoint
- **THEN** every completed task records the authenticated actor as the completing actor
- **THEN** the request does not require or accept a separate actor identifier

#### Scenario: Self-service preparation acts on the authenticated employee context across all assigned projects
- **WHEN** an authenticated employee prepares monthend obligations through the REST API
- **THEN** the API uses the authenticated employee as the acting user for preparation
- **THEN** the request does not require a project identifier — the system discovers all projects the employee is assigned to
- **THEN** the request requires a non-blank clarification text

#### Scenario: Capacity-scoped reads are resolved for the authenticated caller
- **WHEN** an authenticated actor requests the status overview or the payroll month in either capacity
- **THEN** the API resolves the data for the authenticated actor
- **THEN** the request does not require or accept an actor identifier

### Requirement: Monthend endpoint access follows employee, project-lead, and ops roles
The system SHALL secure monthend REST endpoints by operation. The status overview and payroll month endpoints MUST require the role of the requested capacity. The clarification and preparation creation endpoints MUST require at least the employee role. The employee bulk task completion endpoint MUST require the employee role. The project-lead bulk task completion endpoint MUST require the project-lead role. Internal generation endpoints MUST require the internal sync or cron role defined for operational endpoints.

#### Scenario: Unauthenticated caller cannot access monthend endpoints
- **WHEN** an unauthenticated caller requests any actor-scoped monthend endpoint
- **THEN** the API rejects the request as unauthorized

#### Scenario: Employee can access the employee status overview endpoint
- **WHEN** an authenticated employee requests `GET /monthend/{month}/status-overview?capacity=EMPLOYEE`
- **THEN** the API accepts the request and returns the employee-capacity overview

#### Scenario: Non-project-lead cannot access the project-lead status overview endpoint
- **WHEN** an authenticated actor without the project-lead role requests `GET /monthend/{month}/status-overview?capacity=CONTROLLING`
- **THEN** the API rejects the request as forbidden

#### Scenario: Non-project-lead cannot access the payroll month in the controlling capacity
- **WHEN** an authenticated actor without the project-lead role requests `GET /monthend/payroll-month?capacity=CONTROLLING`
- **THEN** the API rejects the request as forbidden

#### Scenario: Project lead can access both status overview endpoints
- **WHEN** an authenticated project lead requests `GET /monthend/{month}/status-overview?capacity=CONTROLLING`
- **THEN** the API accepts the request and returns the controlling-capacity overview
- **WHEN** the same project lead requests `GET /monthend/{month}/status-overview?capacity=EMPLOYEE`
- **THEN** the API accepts the request and returns the employee-capacity overview scoped to that lead

#### Scenario: Non-project-lead cannot access the project-lead bulk task completion endpoint
- **WHEN** an authenticated actor without the project-lead role requests `POST /monthend/{month}/tasks/complete/project-lead`
- **THEN** the API rejects the request as forbidden

#### Scenario: Caller without the employee role cannot access the employee bulk task completion endpoint
- **WHEN** an authenticated caller without the employee role requests `POST /monthend/{month}/tasks/complete/employee`
- **THEN** the API rejects the request as forbidden

#### Scenario: Project lead can access both bulk task completion endpoints
- **WHEN** an authenticated project lead requests `POST /monthend/{month}/tasks/complete/project-lead`
- **THEN** the API accepts the request and evaluates it against the controlling scope
- **WHEN** the same project lead requests `POST /monthend/{month}/tasks/complete/employee`
- **THEN** the API accepts the request and completes only time-check tasks for which the lead is the subject employee

#### Scenario: Employee can access shared monthend endpoint
- **WHEN** an authenticated employee requests a shared actor-scoped monthend endpoint
- **THEN** the API accepts the request and evaluates the action against the existing monthend eligibility rules

#### Scenario: Project lead can access shared monthend endpoint
- **WHEN** an authenticated project lead requests a shared actor-scoped monthend endpoint
- **THEN** the API accepts the request and evaluates the action against the existing monthend eligibility rules

#### Scenario: Non-ops caller cannot access generation endpoint
- **WHEN** an authenticated caller without the internal sync or cron role requests `POST /monthend/{month}/generate`
- **THEN** the API rejects the request as forbidden

## REMOVED Requirements

### Requirement: Monthend status overview is available via explicit role-suffixed paths
**Reason**: The employee and project-lead overviews return the same model and differ only in scope. The scope is now selected by the `capacity` query parameter of a single endpoint. The clarification-creation scenario this requirement carried duplicates the clarification creation requirement, which keeps covering it.
**Migration**: Replace `GET /monthend/{month}/status-overview/employee` with `GET /monthend/{month}/status-overview?capacity=EMPLOYEE`, and `GET /monthend/{month}/status-overview/project-lead` with `GET /monthend/{month}/status-overview?capacity=CONTROLLING`. Expect `canComplete` to also depend on the requested capacity.

### Requirement: Payroll month is available via role-suffixed endpoints
**Reason**: Replaced by a single endpoint whose resolution rule is selected by the `capacity` query parameter.
**Migration**: Replace `GET /monthend/payroll-month/employee` with `GET /monthend/payroll-month?capacity=EMPLOYEE`, and `GET /monthend/payroll-month/project-lead` with `GET /monthend/payroll-month?capacity=CONTROLLING`.
