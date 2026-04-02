## 1. Shared Month-End Foundations

- [ ] 1.1 Extract shared employee-owned month-end task planning logic so scheduled generation and self-service preparation use the same obligation rules
- [ ] 1.2 Extend month-end task persistence with targeted business-key lookup or ensure support for one employee project context
- [ ] 1.3 Add Liquibase changes and persistence handling to enforce `MonthEndTask` business-key uniqueness for both employee-owned and subjectless tasks
- [ ] 1.4 Add hexagon-side lookup and actor-resolution support needed to resolve the logged-in employee into the month-end `UserId`

## 2. Self-Service Preparation Workflow

- [ ] 2.1 Implement the self-service month-end preparation use case for one `(month, project, subject employee)` context
- [ ] 2.2 Restrict the workflow to the subject employee and ensure it creates only `EMPLOYEE_TIME_CHECK` and billable-project `LEISTUNGSNACHWEIS` tasks
- [ ] 2.3 Integrate optional clarification creation in the same workflow while keeping `MonthEndTask` and `MonthEndClarification` as separate aggregates
- [ ] 2.4 Update scheduled month-end generation to coexist with self-service prepared tasks and skip duplicate obligations

## 3. Use Case Wiring and Verification

- [ ] 3.1 Wire the self-service preparation use case to the logged-in-actor resolution support without introducing a REST controller yet
- [ ] 3.2 Add unit tests for authorization, billable vs non-billable behavior, idempotent repetition, and optional clarification creation
- [ ] 3.3 Add integration tests covering self-service preparation before scheduled generation and duplicate-free scheduled follow-up
- [ ] 3.4 Add integration coverage that prepared tasks remain completable through the existing flow and that optional clarifications appear through the existing worklist visibility rules
