## 1. Domain Model

- [x] 1.1 Change `LEISTUNGSNACHWEIS` completion policy in `MonthEndTaskType` from `INDIVIDUAL_ACTOR` to `ANY_ELIGIBLE_ACTOR`
- [x] 1.2 Move `LEISTUNGSNACHWEIS` out of the `EMPLOYEE_TIME_CHECK, LEISTUNGSNACHWEIS` validation branch in `MonthEndTask` — it no longer requires `subjectEmployeeId` to be in `eligibleActorIds`
- [x] 1.3 Add a `LEISTUNGSNACHWEIS` validation branch in `MonthEndTask` matching the `PROJECT_LEAD_REVIEW` invariant: `subjectEmployeeId` required, eligible actors are leads (not the subject)

## 2. Planning Service

- [x] 2.1 Remove `LEISTUNGSNACHWEIS` task creation from `MonthEndTaskPlanningService.planEmployeeOwnedTasks()`
- [x] 2.2 Add `LEISTUNGSNACHWEIS` task creation to `MonthEndTaskPlanningService.planProjectTasks()` inside the per-employee loop, guarded by `project.billable() && !activeLeadIds.isEmpty()`, using `activeLeadIds` as eligible actors

## 3. Self-Service Preparation

- [x] 3.1 Remove `LEISTUNGSNACHWEIS` task creation from `PrematureMonthEndPreparationService` (self-service preparation no longer generates LEISTUNGSNACHWEIS)

## 4. Unit Tests — Domain Model

- [x] 4.1 Update `MonthEndTaskTest` validation tests: LEISTUNGSNACHWEIS no longer requires `subjectEmployeeId` in `eligibleActorIds`; add test that a LEISTUNGSNACHWEIS task with lead as eligible actor (not the subject employee) is valid

## 5. Unit Tests — Planning Service

- [x] 5.1 Update `MonthEndTaskPlanningServiceTest`: `planEmployeeOwnedTasks()` no longer returns a LEISTUNGSNACHWEIS task for billable projects
- [x] 5.2 Update `MonthEndTaskPlanningServiceTest`: `planProjectTasks()` returns a LEISTUNGSNACHWEIS task per employee on a billable project with lead IDs as eligible actors
- [x] 5.3 Add test to `MonthEndTaskPlanningServiceTest`: `planProjectTasks()` does not create LEISTUNGSNACHWEIS when there are no active leads on a billable project
- [x] 5.4 Add test to `MonthEndTaskPlanningServiceTest`: `planProjectTasks()` does not create LEISTUNGSNACHWEIS for non-billable projects

## 6. Unit Tests — Self-Service Preparation

- [x] 6.1 Update `PrematureMonthEndPreparationServiceTest`: billable project self-service no longer creates a LEISTUNGSNACHWEIS task

## 7. Unit Tests — Generation Service

- [x] 7.1 Update `GenerateMonthEndTasksServiceTest`: verify LEISTUNGSNACHWEIS tasks are created with lead IDs as eligible actors during scheduled generation
- [x] 7.2 Update `GenerateMonthEndTasksServiceTest`: verify LEISTUNGSNACHWEIS is not created for non-billable projects

## 8. Adapter Tests

- [x] 8.1 Review `MonthEndTaskRepositoryAdapterTest` for any LEISTUNGSNACHWEIS-specific assertions that assume employee as eligible actor and update accordingly
- [x] 8.2 Review `PayrollMonthCompletionAdapterTest` for any LEISTUNGSNACHWEIS-specific assumptions and update accordingly

## 9. Integration Tests

- [x] 9.1 Update `MonthEndIT` to reflect that LEISTUNGSNACHWEIS tasks are now completed by leads, not employees

## 10. Spec Sync

- [x] 10.1 Run `/opsx:sync` to merge all delta specs into the main specs under `openspec/specs/`
