## MODIFIED Requirements

### Requirement: Work time data is not embedded in the monthend status overview response
The monthend status overview endpoint SHALL NOT include work time aggregations in its response. Work time per payroll month SHALL be fetched via the dedicated `/worktime/{payrollMonth}` endpoint in the same capacity as the overview. The frontend SHALL call the work time endpoint independently to obtain billable/non-billable hour breakdowns alongside the monthend overview.

#### Scenario: Monthend overview response contains no work time fields
- **WHEN** an authenticated actor requests the monthend status overview
- **THEN** the response does NOT contain billable hours, non-billable hours, or work time entries
- **THEN** work time data is available exclusively through `GET /worktime/{payrollMonth}?capacity=EMPLOYEE|CONTROLLING`
