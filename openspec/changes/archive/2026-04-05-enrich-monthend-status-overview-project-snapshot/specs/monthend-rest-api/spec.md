## MODIFIED Requirements

### Requirement: Shared monthend endpoints expose status overview and monthend actions
The system SHALL provide shared monthend REST endpoints that allow authenticated employee or project-lead actors to retrieve a unified actor-centric status overview, complete monthend tasks, edit open clarification text when they are on the creator side, and resolve clarifications when they are on the resolver side. The shared status overview response SHALL expose project display data needed by the UI through a nested project object for each overview entry.

#### Scenario: Employee retrieves unified status overview
- **WHEN** an authenticated employee requests the shared monthend status overview for a month
- **THEN** the API returns the actor-centric overview for that employee
- **THEN** the overview contains both open and completed monthend tasks relevant to that actor
- **THEN** each overview entry includes a nested project object containing the project identifier and project name

#### Scenario: Project lead retrieves unified status overview
- **WHEN** an authenticated project lead requests the shared monthend status overview for a month
- **THEN** the API returns the actor-centric overview for that lead
- **THEN** the overview contains both open and completed monthend tasks relevant to that actor
- **THEN** each overview entry includes a nested project object containing the project identifier and project name

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
