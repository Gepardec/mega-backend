# monthend-rest-api Specification

## Purpose
TBD - created by archiving change add-spec-first-monthend-api. Update Purpose after archive.
## Requirements
### Requirement: Monthend REST API contract is defined spec-first
The system SHALL define the monthend REST API in a single canonical OpenAPI document. Java API interfaces and HTTP models for monthend endpoints SHALL be generated from that contract and used by the REST adapter implementation.

#### Scenario: Generated API types follow the canonical contract
- **WHEN** the monthend REST contract is updated
- **THEN** the OpenAPI document is updated as the canonical source
- **THEN** the generated Java API interfaces and models reflect that contract for the monthend REST adapter layer

#### Scenario: Monthend REST adapters implement generated interfaces
- **WHEN** the monthend REST API is implemented
- **THEN** handwritten monthend REST adapters implement the generated Java API interfaces instead of defining separate handwritten endpoint signatures

### Requirement: Actor-scoped monthend endpoints derive the acting user from authentication
The system SHALL treat the authenticated caller as the acting monthend actor for all actor-scoped monthend REST endpoints. Actor-scoped requests MUST NOT require a caller-supplied actor identifier for employee worklists, project-lead worklists, shared status overview, task completion, clarification edit, clarification resolve, or employee self-service preparation.

#### Scenario: Employee worklist is resolved for the authenticated employee
- **WHEN** an authenticated employee requests the employee monthend worklist for a month
- **THEN** the API resolves the acting employee from the authentication context
- **THEN** the request does not require an employee identifier

#### Scenario: Task completion is attributed to the authenticated caller
- **WHEN** an authenticated eligible actor completes a monthend task through the REST API
- **THEN** the completion uses the authenticated actor as the acting user
- **THEN** the request does not require a separate actor identifier

#### Scenario: Self-service preparation acts on the authenticated employee context
- **WHEN** an authenticated employee prepares monthend obligations through the REST API
- **THEN** the API uses the authenticated employee as the acting user for preparation
- **THEN** the request does not require a separate actor identifier

### Requirement: Employee monthend endpoints expose employee worklist and self-service flows
The system SHALL provide employee-scoped monthend REST endpoints that allow authenticated employees to retrieve their monthend worklist, prepare their own project monthend context, and create clarifications for their own monthend project context. Responses SHALL expose the generated task and clarification models needed by the employee client.

#### Scenario: Employee retrieves their monthend worklist
- **WHEN** an authenticated employee requests the employee monthend worklist for a month
- **THEN** the API returns the employee's open monthend tasks for that month
- **THEN** the API returns the employee-visible open clarifications for that month

#### Scenario: Employee prepares a project context with optional clarification
- **WHEN** an authenticated employee submits a preparation request for one project and month with optional clarification text
- **THEN** the API ensures the employee-owned monthend obligations for that project context exist
- **THEN** the API includes the ensured tasks and the created clarification when clarification text was provided

#### Scenario: Employee creates a clarification for their own project context
- **WHEN** an authenticated employee submits a clarification creation request for their own monthend project context
- **THEN** the API creates an employee-side monthend clarification
- **THEN** the API returns the created clarification in the generated response model

### Requirement: Project-lead monthend endpoints expose lead worklist and clarification creation
The system SHALL provide project-lead-scoped monthend REST endpoints that allow authenticated project leads to retrieve their lead worklist and create clarifications for a subject employee in the same monthend project context. Responses SHALL expose the generated task and clarification models needed by the lead client.

#### Scenario: Project lead retrieves the lead monthend worklist
- **WHEN** an authenticated project lead requests the project-lead monthend worklist for a month
- **THEN** the API returns the lead-visible open monthend tasks for that month
- **THEN** the API returns the lead-visible open clarifications for that month

#### Scenario: Project lead creates a clarification for a subject employee
- **WHEN** an authenticated eligible project lead submits a clarification creation request for a subject employee in a monthend project context
- **THEN** the API creates a project-lead-side monthend clarification
- **THEN** the API returns the created clarification in the generated response model

### Requirement: Shared monthend endpoints expose status overview and monthend actions
The system SHALL provide shared monthend REST endpoints that allow authenticated employee or project-lead actors to retrieve a unified actor-centric status overview, complete monthend tasks, edit open clarification text when they are on the creator side, and resolve clarifications when they are on the resolver side.

#### Scenario: Employee retrieves unified status overview
- **WHEN** an authenticated employee requests the shared monthend status overview for a month
- **THEN** the API returns the actor-centric overview for that employee
- **THEN** the overview contains both open and completed monthend tasks relevant to that actor

#### Scenario: Project lead retrieves unified status overview
- **WHEN** an authenticated project lead requests the shared monthend status overview for a month
- **THEN** the API returns the actor-centric overview for that lead
- **THEN** the overview contains both open and completed monthend tasks relevant to that actor

#### Scenario: Eligible actor completes a monthend task
- **WHEN** an authenticated eligible actor submits a task completion request
- **THEN** the API completes the monthend task through the existing completion flow
- **THEN** the API returns the resulting task state including completion metadata when present

#### Scenario: Creator side edits an open clarification
- **WHEN** an authenticated actor on the creator side submits a clarification text update for an open clarification
- **THEN** the API updates the clarification text through the existing clarification update flow
- **THEN** the API returns the updated clarification state

#### Scenario: Resolver side resolves a clarification
- **WHEN** an authenticated actor allowed to resolve a clarification submits a clarification resolution request with an optional resolution note
- **THEN** the API resolves the clarification through the existing clarification completion flow
- **THEN** the API returns the resolved clarification state

### Requirement: Monthend endpoint access follows employee, project-lead, and ops roles
The system SHALL secure monthend REST endpoints by endpoint group. Employee-scoped endpoints MUST require the employee role, project-lead-scoped endpoints MUST require the project-lead role, shared actor-scoped endpoints MUST require either employee or project-lead role, and internal generation endpoints MUST require the internal sync or cron role defined for operational endpoints.

#### Scenario: User without employee role cannot access employee-scoped endpoint
- **WHEN** an authenticated caller without the employee role requests an employee-scoped monthend endpoint
- **THEN** the API rejects the request as forbidden

#### Scenario: User without project-lead role cannot access lead-scoped endpoint
- **WHEN** an authenticated caller without the project-lead role requests a project-lead-scoped monthend endpoint
- **THEN** the API rejects the request as forbidden

#### Scenario: Employee can access shared monthend endpoint
- **WHEN** an authenticated employee requests a shared actor-scoped monthend endpoint
- **THEN** the API accepts the request and evaluates the action against the existing monthend eligibility rules

#### Scenario: Project lead can access shared monthend endpoint
- **WHEN** an authenticated project lead requests a shared actor-scoped monthend endpoint
- **THEN** the API accepts the request and evaluates the action against the existing monthend eligibility rules

#### Scenario: Non-ops caller cannot access generation endpoint
- **WHEN** an authenticated caller without the internal sync or cron role requests the monthend generation endpoint
- **THEN** the API rejects the request as forbidden

### Requirement: Internal monthend generation is available through the same API contract
The system SHALL provide an internal monthend generation endpoint in the same OpenAPI contract for operational callers. The endpoint SHALL trigger monthend task generation for the requested month and return the generation result using generated response models.

#### Scenario: Ops caller triggers generation for a month
- **WHEN** an authenticated internal ops caller submits a monthend generation request for a month
- **THEN** the API triggers monthend task generation for that month
- **THEN** the API returns the generation result including created and skipped counts

