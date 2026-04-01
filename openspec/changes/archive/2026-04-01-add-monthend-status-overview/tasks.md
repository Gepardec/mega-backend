## 1. Query Model

- [x] 1.1 Introduce a dedicated month-end status-overview read model and use case for the matrix/dashboard view by actor and month.
- [x] 1.2 Implement an application query service that returns both `OPEN` and `DONE` month-end tasks relevant to the requesting actor and month.
- [x] 1.3 Keep the existing employee and lead worklist contracts unchanged and open-only while adding the separate overview flow.
- [x] 1.4 Extend the repository contract and query logic needed to load overview-relevant tasks, including completed shared lead tasks.

## 2. Mapping

- [x] 2.1 Reuse or extend the query-side mapping needed to expose task identity, task type, task status, project reference, subject employee reference, and `completedBy` in overview entries.
- [x] 2.2 Keep the change free of REST resources, controllers, and other inbound delivery adapters.

## 3. Verification

- [x] 3.1 Add unit tests for mixed open/done overview scenarios, including completed shared lead tasks that remain visible in the overview.
- [x] 3.2 Add integration coverage showing that the status overview includes completed tasks while the existing worklist behavior remains open-only.
