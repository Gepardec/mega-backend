## 1. Rename shared projection records

- [x] 1.1 Rename `MonthEndStatusOverviewProject` → `MonthEndProject` (file rename + class rename)
- [x] 1.2 Rename `MonthEndStatusOverviewSubjectEmployee` → `MonthEndEmployee` (file rename + class rename)
- [x] 1.3 Update `MonthEndStatusOverviewItem` imports and field types to use the new names

## 2. Update MonthEndWorklistItem

- [x] 2.1 Replace `ProjectId projectId` with `MonthEndProject project` in `MonthEndWorklistItem`
- [x] 2.2 Replace `UserId subjectEmployeeId` with `MonthEndEmployee subjectEmployee` in `MonthEndWorklistItem`

## 3. Update OpenAPI spec

- [x] 3.1 In `monthend.openapi.yaml`, update the `MonthEndWorklistTask` schema: replace `projectId: uuid` and `subjectEmployeeId: uuid` with `project: $ref MonthEndProjectReference` (required) and `subjectEmployee: $ref MonthEndSubjectEmployeeReference` (nullable)
- [x] 3.2 Regenerate sources so the updated `MonthEndWorklistTask` Java class is produced (`mvn generate-sources` or equivalent)

## 4. Update mappers

- [x] 4.1 Update `MonthEndStatusOverviewMapper` — change return types / parameter types to `MonthEndProject` and `MonthEndEmployee`
- [x] 4.2 Update `MonthEndWorklistMapper.toItem()` to map `MonthEndTask` → `MonthEndWorklistItem` with embedded `MonthEndProject` and `MonthEndEmployee` (requires project name and employee full name to be available from the task snapshot; add mapping methods analogous to those in `MonthEndStatusOverviewMapper`)
- [x] 4.3 Update `MonthEndRestMapper` — fix import references to `MonthEndProject` / `MonthEndEmployee`; add/update mapping methods for `MonthEndProject` → `MonthEndProjectReference` and `MonthEndEmployee` → `MonthEndSubjectEmployeeReference` for the worklist task path

## 5. Update tests

- [x] 5.1 Update `MonthEndRestMapperTest` — replace `MonthEndWorklistItem(taskId, type, projectId, employeeId)` with `MonthEndProject`/`MonthEndEmployee` instances; update assertions from `getProjectId()`/`getSubjectEmployeeId()` to `getProject().getId()`/`getSubjectEmployee().getId()`
- [x] 5.2 Update `MonthEndRestMapperTest` — replace `MonthEndStatusOverviewProject` / `MonthEndStatusOverviewSubjectEmployee` with `MonthEndProject` / `MonthEndEmployee`
- [x] 5.3 Update `MonthEndEmployeeAndProjectLeadResourceTest` — replace `MonthEndWorklistItem(taskId, type, projectId, employeeId)` with enriched constructors; update assertions from `getProjectId()`/`getSubjectEmployeeId()` to `getProject().getId()`/`getSubjectEmployee().getId()`
- [x] 5.4 Update `MonthEndSharedResourceTest` — replace `MonthEndStatusOverviewProject` / `MonthEndStatusOverviewSubjectEmployee` with `MonthEndProject` / `MonthEndEmployee`
- [x] 5.5 Update `MonthEndStatusOverviewServiceTest` — replace `MonthEndStatusOverviewProject` / `MonthEndStatusOverviewSubjectEmployee` with `MonthEndProject` / `MonthEndEmployee`
- [x] 5.6 Update `MonthEndIT` — update worklist item construction and field accessors (`projectId()` → `project().id()`, `subjectEmployeeId()` → `subjectEmployee().id()`)

## 6. Verify

- [x] 6.1 Run `mvn test` and confirm all tests pass
