## 1. Domain Model

- [ ] 1.1 Create `WorkTimeEntry` record with `EmployeeRef`, `ProjectRef`, `billableHours` (double), `nonBillableHours` (double), `employeeMonthTotalHours` (double)
- [ ] 1.2 Create `EmployeeRef` value object with `UserId id` and `String name`
- [ ] 1.3 Create `ProjectRef` value object with `ProjectId id` and `String name`
- [ ] 1.4 Create `WorkTimeReport` record with `YearMonth payrollMonth` and `List<WorkTimeEntry> entries`

## 2. Outbound ZEP Port and Adapter

- [ ] 2.1 Create `WorkTimeZepPort` outbound port interface with:
  - `fetchAttendancesForEmployee(String zepEmployeeId, YearMonth month) → List<WorkTimeAttendance>`
  - `fetchProjectMembershipForMonth(Integer zepProjectId, YearMonth month) → List<String>` (returns ZEP employee IDs)
- [ ] 2.2 Create `WorkTimeAttendance` value object with `employeeZepId`, `projectZepId`, `billableHours`, `nonBillableHours`
- [ ] 2.3 Implement `WorkTimeZepAdapter`:
  - `fetchAttendancesForEmployee` delegates to `AttendanceService.getAttendanceForUserAndMonth`; maps `ZepAttendance` to `WorkTimeAttendance` (sum billable durations separately from non-billable per record)
  - `fetchProjectMembershipForMonth` delegates to `AttendanceService.getAttendanceForUserProjectAndMonth` or a membership-specific ZEP call; returns distinct employee IDs who booked time on that project

## 3. Use Cases

- [ ] 3.1 Define `GetEmployeeWorkTimeUseCase` inbound port: `getWorkTime(UserId employeeId, YearMonth month) → WorkTimeReport`
- [ ] 3.2 Define `GetProjectLeadWorkTimeUseCase` inbound port: `getWorkTime(UserId callerId, YearMonth month) → WorkTimeReport`
- [ ] 3.3 Implement `GetEmployeeWorkTimeService`:
  - Resolve ZEP employee ID from `UserRepository`
  - Call `WorkTimeZepPort.fetchAttendancesForEmployee` (single ZEP call)
  - Group attendances by ZEP project ID; resolve project display names from `ProjectRepository`
  - Compute `employeeMonthTotalHours` as sum of all attendance durations
  - Build `WorkTimeReport`
- [ ] 3.4 Implement `GetProjectLeadWorkTimeService` (three-phase approach):
  - **Phase 1 (DB)**: `ProjectRepository.findAllByLead(callerId)` → all projects with their ZEP IDs
  - **Phase 2 (ZEP, parallel)**: `WorkTimeZepPort.fetchProjectMembershipForMonth` for each project → union of unique ZEP employee IDs
  - **Phase 3 (ZEP, parallel)**: `WorkTimeZepPort.fetchAttendancesForEmployee` for each unique ZEP employee ID → complete attendance data for the month
  - **Merge**: filter Phase 3 attendances to lead's project ZEP IDs for entry data; sum all Phase 3 durations per employee for `employeeMonthTotalHours`
  - Resolve employee display names from `UserRepository`; build `WorkTimeReport`

## 4. OpenAPI Contract

- [ ] 4.1 Create a single canonical root contract at `src/main/resources/openapi/openapi.yaml` and split the source tree into referenced `paths/`, `schemas/`, `parameters/`, and `responses/` files
  - Add worktime path definitions for `GET /worktime/employee/{payrollMonth}` and `GET /worktime/projects/{payrollMonth}`
  - Add worktime schemas for `WorkTimeReportResponse`, `WorkTimeEntryResponse`, and nested employee/project references
  - Fold the existing monthend contract into the same source tree so both bounded contexts share one canonical application contract
- [ ] 4.2 Add build-time linting and bundling for the root OpenAPI source tree; generate a bundled artifact that becomes the only input to code generation
- [ ] 4.3 Generate Java interfaces and models from the bundled root artifact in an application-level generated package; update existing monthend imports if needed so generated transport types are no longer monthend-package-specific
- [ ] 4.4 Verify generated Java interfaces and models are present in `target/generated-sources`

## 5. REST Adapter

- [ ] 5.1 Create `WorkTimeEmployeeResource` implementing generated `WorkTimeEmployeeApi`; annotate with `@MegaRolesAllowed(EMPLOYEE)`; resolve caller `UserId` from auth context; delegate to `GetEmployeeWorkTimeUseCase`
- [ ] 5.2 Create `WorkTimeProjectLeadResource` implementing generated `WorkTimeProjectLeadApi`; annotate with `@MegaRolesAllowed(PROJECT_LEAD)`; resolve caller `UserId` from auth context; delegate to `GetProjectLeadWorkTimeUseCase`
- [ ] 5.3 Create `WorkTimeRestMapper` (MapStruct) mapping `WorkTimeReport` → generated `WorkTimeReportResponse`

## 6. Tests

- [ ] 6.1 Unit test `GetEmployeeWorkTimeService`: verify correct grouping by project, billable/non-billable aggregation, correct `employeeMonthTotalHours`, empty report for no attendances
- [ ] 6.2 Unit test `GetProjectLeadWorkTimeService`: verify three-phase fetch orchestration, correct (employee × project) entry creation, correct `employeeMonthTotalHours` spanning all projects (not just lead's), empty report for no managed projects, empty report for no bookings
- [ ] 6.3 Unit test `WorkTimeZepAdapter`: verify `WorkTimeAttendance` mapping (billable flag splits durations correctly)
- [ ] 6.4 Integration test `WorkTimeEmployeeResource` (`@QuarkusTest`): verify response shape, `employeeMonthTotalHours` present, EMPLOYEE role required
- [ ] 6.5 Integration test `WorkTimeProjectLeadResource` (`@QuarkusTest`): verify response covers all lead's projects, `employeeMonthTotalHours` reflects full employee totals, PROJECT_LEAD role required
