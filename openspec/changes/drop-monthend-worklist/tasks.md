## 1. Delete Domain Models

- [ ] 1.1 Delete `MonthEndWorklist.java`
- [ ] 1.2 Delete `MonthEndWorklistItem.java`
- [ ] 1.3 Delete `MonthEndWorklistClarificationItem.java`

## 2. Delete Application Layer

- [ ] 2.1 Delete `GetEmployeeMonthEndWorklistUseCase.java` (inbound port)
- [ ] 2.2 Delete `GetProjectLeadMonthEndWorklistUseCase.java` (inbound port)
- [ ] 2.3 Delete `GetEmployeeMonthEndWorklistService.java`
- [ ] 2.4 Delete `GetProjectLeadMonthEndWorklistService.java`

## 3. Delete Tests

- [ ] 3.1 Delete `MonthEndWorklistServicesTest.java`

## 4. Update REST Resources

- [ ] 4.1 Remove `getEmployeeMonthEndWorklist()` method and its `GetEmployeeMonthEndWorklistUseCase` dependency from `MonthEndEmployeeResource`
- [ ] 4.2 Remove `getProjectLeadMonthEndWorklist()` method and its `GetProjectLeadMonthEndWorklistUseCase` dependency from `MonthEndProjectLeadResource`

## 5. Update REST Mapper

- [ ] 5.1 Remove `toResponse(MonthEndWorklist worklist)` from `MonthEndRestMapper`
- [ ] 5.2 Remove `toResponse(MonthEndWorklistItem item)` from `MonthEndRestMapper`
- [ ] 5.3 Remove `toResponse(MonthEndWorklistClarificationItem item)` from `MonthEndRestMapper`

## 6. Update OpenAPI Contract

- [ ] 6.1 Remove `GET /monthend/employee/worklist` path entry from `paths/monthend.yaml`
- [ ] 6.2 Remove `GET /monthend/project-lead/worklist` path entry from `paths/monthend.yaml`
- [ ] 6.3 Remove `MonthEndWorklistTask` schema from `schemas/monthend.yaml`
- [ ] 6.4 Remove `MonthEndWorklistClarification` schema from `schemas/monthend.yaml`
- [ ] 6.5 Remove `MonthEndWorklistResponse` schema from `schemas/monthend.yaml`
- [ ] 6.6 Regenerate OpenAPI-generated Java sources (`mvn generate-sources`) and verify no compile errors

## 7. Update Specs

- [ ] 7.1 Delete `openspec/specs/monthend-task-worklist/spec.md`
- [ ] 7.2 Apply delta to `openspec/specs/monthend-rest-api/spec.md` — remove worklist requirements and scenarios
- [ ] 7.3 Apply delta to `openspec/specs/monthend-clarifications/spec.md` — remove worklist reference from visibility scenario and last sentence of repository requirement
- [ ] 7.4 Apply delta to `openspec/specs/monthend-status-overview/spec.md` — remove "Worklists remain focused..." requirement
- [ ] 7.5 Apply delta to `openspec/specs/shared-user-project-refs/spec.md` — remove `MonthEndWorklistItem` from UserRef and ProjectRef scenarios
