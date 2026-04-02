## 1. Shared Month-End Foundations

- [x] 1.1 Extract shared employee-owned month-end task planning logic so scheduled generation and self-service preparation use the same obligation rules
- [x] 1.2 Extend month-end task persistence with targeted business-key lookup or ensure support for one employee project context
- [x] 1.3 Add Liquibase changes and persistence handling to enforce `MonthEndTask` business-key uniqueness for both employee-owned and subjectless tasks
- [x] 1.4 Add hexagon-side lookup and actor-resolution support needed to resolve the logged-in employee into the month-end `UserId`

## 2. Self-Service Preparation Workflow

- [x] 2.1 Implement the self-service month-end preparation use case for one `(month, project, subject employee)` context
- [x] 2.2 Restrict the workflow to the subject employee and ensure it creates only `EMPLOYEE_TIME_CHECK` and billable-project `LEISTUNGSNACHWEIS` tasks
- [x] 2.3 Integrate optional clarification creation in the same workflow while keeping `MonthEndTask` and `MonthEndClarification` as separate aggregates
- [x] 2.4 Update scheduled month-end generation to coexist with self-service prepared tasks and skip duplicate obligations

## 3. Use Case Wiring and Verification

- [x] 3.1 Wire the self-service preparation use case to the logged-in-actor resolution support without introducing a REST controller yet
- [x] 3.2 Add unit tests for authorization, billable vs non-billable behavior, idempotent repetition, and optional clarification creation
- [x] 3.3 Add integration tests covering self-service preparation before scheduled generation and duplicate-free scheduled follow-up
- [x] 3.4 Add integration coverage that prepared tasks remain completable through the existing flow and that optional clarifications appear through the existing worklist visibility rules
