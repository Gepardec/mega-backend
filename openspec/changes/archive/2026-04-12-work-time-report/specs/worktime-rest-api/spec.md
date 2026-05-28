## ADDED Requirements

### Requirement: Work time REST API contract is defined spec-first
The system SHALL define the work time REST API as part of the single canonical application OpenAPI contract. The source contract MAY be split across referenced files and SHALL be bundled for code generation. Java API interfaces and HTTP models for work time endpoints SHALL be generated from that bundled contract and used by the REST adapter implementation.

#### Scenario: Generated API types follow the canonical contract
- **WHEN** the work time REST contract is updated
- **THEN** the OpenAPI document is updated as the canonical source
- **THEN** the generated Java API interfaces and models reflect that contract for the worktime REST adapter layer

#### Scenario: Work time REST adapters implement generated interfaces
- **WHEN** a work time REST adapter is implemented
- **THEN** it implements the generated Java API interface instead of defining a separate handwritten endpoint signature

### Requirement: Work time endpoints derive the acting user from authentication
The system SHALL treat the authenticated caller as the acting subject for all work time REST endpoints. Work time requests MUST NOT require a caller-supplied actor identifier; the acting user SHALL be resolved from the authentication context.

#### Scenario: Employee work time is resolved for the authenticated employee
- **WHEN** an authenticated employee requests the employee work time report for a month
- **THEN** the API resolves the acting employee from the authentication context
- **THEN** the request does not require an employee identifier

#### Scenario: Project lead work time is resolved for the authenticated project lead
- **WHEN** an authenticated project lead requests the project lead work time report for a month
- **THEN** the API resolves the acting project lead from the authentication context
- **THEN** the request does not require a user identifier

### Requirement: Work time endpoint access follows employee and project-lead roles
The system SHALL secure work time REST endpoints by caller role. The employee work time endpoint MUST require the EMPLOYEE role. The project lead work time endpoint MUST require the PROJECT_LEAD role.

#### Scenario: Caller without EMPLOYEE role cannot access the employee work time endpoint
- **WHEN** an authenticated caller without the EMPLOYEE role requests the employee work time endpoint
- **THEN** the API rejects the request as forbidden

#### Scenario: Caller without PROJECT_LEAD role cannot access the project lead work time endpoint
- **WHEN** an authenticated caller without the PROJECT_LEAD role requests the project lead work time endpoint
- **THEN** the API rejects the request as forbidden
