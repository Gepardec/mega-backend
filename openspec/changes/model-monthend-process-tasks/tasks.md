## 1. Replace the month-end write model

- [ ] 1.1 Introduce the `MonthEndTask` aggregate, task type terminology, and completion policy model in the month-end domain
- [ ] 1.2 Define a unified `MonthEndTask` repository contract and supporting domain ports
- [ ] 1.3 Align the month-end domain classes so they speak only in terms of month-end tasks

## 2. Replace persistence and adapters

- [ ] 2.1 Replace the month-end schema with unified `monthend_task` persistence and eligible-actor storage
- [ ] 2.2 Implement persistence entities, MapStruct mappers, and repository adapters for the unified task model
- [ ] 2.3 Align persistence adapters and schema elements with the unified task model

## 3. Rebuild month-end use cases on the new model

- [ ] 3.1 Implement month-end generation so it creates unified tasks for employee-owned and project-owned obligations
- [ ] 3.2 Implement a unified month-end task completion use case that delegates completion rules to the aggregate
- [ ] 3.3 Introduce month-end worklist query use cases for employee and project-lead views over open tasks

## 4. Update tests and verification

- [ ] 4.1 Rewrite domain tests around `MonthEndTask` invariants and completion behavior
- [ ] 4.2 Rewrite application tests for generation, completion, and worklist queries using the new model
- [ ] 4.3 Align test names, fixtures, and assertions with the month-end task language
- [ ] 4.4 Run focused month-end compile and test verification for the refactored model
