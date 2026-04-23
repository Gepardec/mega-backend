## MODIFIED Requirements

### Requirement: Actor-scoped monthend endpoints derive the acting user from authentication
The system SHALL treat the authenticated caller as the acting monthend actor for all actor-scoped monthend REST endpoints. Actor-scoped requests MUST NOT require a caller-supplied actor identifier for employee status overview, project-lead status overview, task completion, clarification edit, clarification resolve, employee self-service preparation, or clarification deletion.

#### Scenario: Task completion is attributed to the authenticated caller
- **WHEN** an authenticated eligible actor completes a monthend task through the REST API
- **THEN** the completion uses the authenticated actor as the acting user
- **THEN** the request does not require a separate actor identifier

#### Scenario: Self-service preparation acts on the authenticated employee context across all assigned projects
- **WHEN** an authenticated employee prepares monthend obligations through the REST API
- **THEN** the API uses the authenticated employee as the acting user for preparation
- **THEN** the request does not require a project identifier — the system discovers all projects the employee is assigned to
- **THEN** the request requires a non-blank clarification text

### Requirement: Employee self-service preparation endpoint accepts month and clarification text only
The system SHALL provide `POST /monthend/preparations` accepting a request body with `month` and `clarificationText` as required fields. The endpoint SHALL NOT accept a `projectId` field. The endpoint SHALL return `204 No Content` on success.

#### Scenario: Employee prepares month-end obligations with clarification
- **WHEN** an authenticated employee submits `{ "month": "2026-03", "clarificationText": "I am leaving before the scheduled run." }` to `POST /monthend/preparations`
- **THEN** the API prepares employee-owned tasks for all projects the employee is assigned to in that month
- **THEN** the API returns `204 No Content`

#### Scenario: Request without clarification text is rejected
- **WHEN** an authenticated employee submits a preparation request without a `clarificationText` field or with a blank value
- **THEN** the API rejects the request with a 400 Bad Request response

## REMOVED Requirements

### Requirement: Employee prepares a project context with optional clarification
**Reason**: Replaced by "Employee self-service preparation endpoint accepts month and clarification text only". The old requirement described a `projectId`-scoped request returning `200` with a `MonthEndPreparationResultDto` body. The endpoint now operates across all assigned projects, requires clarification text, and returns `204 No Content`.
**Migration**: Remove `projectId` from `PrepareMonthEndProjectRequest`. Add `clarificationText` as required. Update client response handling from `200 + body` to `204 No Content`.
