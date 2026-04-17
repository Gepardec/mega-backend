## 1. Delete Domain Models

- [x] 1.1 Delete `MonthEndWorklist.java`
- [x] 1.2 Delete `MonthEndWorklistItem.java`
- [x] 1.3 Delete `MonthEndWorklistClarificationItem.java`

## 2. Delete Application Layer

- [x] 2.1 Delete `GetEmployeeMonthEndWorklistUseCase.java` (inbound port)
- [x] 2.2 Delete `GetProjectLeadMonthEndWorklistUseCase.java` (inbound port)
- [x] 2.3 Delete `GetEmployeeMonthEndWorklistService.java`
- [x] 2.4 Delete `GetProjectLeadMonthEndWorklistService.java`

## 3. Delete Tests

- [x] 3.1 Delete `MonthEndWorklistServicesTest.java`

## 4. Update REST Resources

- [x] 4.1 Remove `getEmployeeMonthEndWorklist()` method and its `GetEmployeeMonthEndWorklistUseCase` dependency from `MonthEndEmployeeResource`
- [x] 4.2 Remove `getProjectLeadMonthEndWorklist()` method and its `GetProjectLeadMonthEndWorklistUseCase` dependency from `MonthEndProjectLeadResource`

## 5. Update REST Mapper

- [x] 5.1 Remove `toResponse(MonthEndWorklist worklist)` from `MonthEndRestMapper`
- [x] 5.2 Remove `toResponse(MonthEndWorklistItem item)` from `MonthEndRestMapper`
- [x] 5.3 Remove `toResponse(MonthEndWorklistClarificationItem item)` from `MonthEndRestMapper`

## 6. Update OpenAPI Contract

- [x] 6.1 Remove `GET /monthend/employee/worklist` path entry from `paths/monthend.yaml`
- [x] 6.2 Remove `GET /monthend/project-lead/worklist` path entry from `paths/monthend.yaml`
- [x] 6.3 Remove `MonthEndWorklistTask` schema from `schemas/monthend.yaml`
- [x] 6.4 Remove `MonthEndWorklistClarification` schema from `schemas/monthend.yaml`
- [x] 6.5 Remove `MonthEndWorklistResponse` schema from `schemas/monthend.yaml`
- [x] 6.6 Regenerate OpenAPI-generated Java sources (`mvn generate-sources`) and verify no compile errors

## 7. Update Specs

- [x] 7.1 Delete `openspec/specs/monthend-task-worklist/spec.md`
- [x] 7.2 Apply delta to `openspec/specs/monthend-rest-api/spec.md` — remove worklist requirements and scenarios
- [x] 7.3 Apply delta to `openspec/specs/monthend-clarifications/spec.md` — remove worklist reference from visibility scenario and last sentence of repository requirement
- [x] 7.4 Apply delta to `openspec/specs/monthend-status-overview/spec.md` — remove "Worklists remain focused..." requirement
- [x] 7.5 Apply delta to `openspec/specs/shared-user-project-refs/spec.md` — remove `MonthEndWorklistItem` from UserRef and ProjectRef scenarios
