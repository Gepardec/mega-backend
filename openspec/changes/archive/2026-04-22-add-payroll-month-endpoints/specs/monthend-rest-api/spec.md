## ADDED Requirements

### Requirement: Payroll month is available via role-suffixed endpoints
The system SHALL provide two role-specific payroll month endpoints — one for the employee view and one for the project-lead view — so that actors holding both roles can independently request either resolved month. `GET /monthend/payroll-month/employee` SHALL return the resolved payroll month for the authenticated employee. `GET /monthend/payroll-month/project-lead` SHALL return the resolved payroll month for the authenticated project lead.

#### Scenario: Employee retrieves their payroll month
- **WHEN** an authenticated employee requests `GET /monthend/payroll-month/employee`
- **THEN** the API returns the resolved payroll month as a `YearMonth` string in `yyyy-MM` format
- **THEN** the response reflects the payroll month resolution rule defined in the `payroll-month` capability

#### Scenario: Project lead retrieves their payroll month
- **WHEN** an authenticated project lead requests `GET /monthend/payroll-month/project-lead`
- **THEN** the API returns the resolved payroll month as a `YearMonth` string in `yyyy-MM` format
- **THEN** the resolved month is the previous calendar month

#### Scenario: Project lead retrieves employee payroll month for their own employee view
- **WHEN** an authenticated project lead requests `GET /monthend/payroll-month/employee`
- **THEN** the API applies the employee rule to the authenticated lead as the subject actor
- **THEN** the response may differ from the result of `GET /monthend/payroll-month/project-lead`

#### Scenario: Unauthenticated caller cannot access payroll month endpoints
- **WHEN** an unauthenticated caller requests either payroll month endpoint
- **THEN** the API rejects the request as unauthorized

#### Scenario: Non-project-lead cannot access the project-lead payroll month endpoint
- **WHEN** an authenticated actor without the project-lead role requests `GET /monthend/payroll-month/project-lead`
- **THEN** the API rejects the request as forbidden
