# Tasks

## 1. API contract (OpenAPI)

- [x] 1.1 In `openapi/paths/monthend.yaml` and `openapi/openapi.yaml`, replace `/monthend/{month}/tasks/complete/project-lead` with `/monthend/{month}/projects/{projectId}/tasks/complete` (adding a required `projectId` path parameter, `format: uuid`), and `/monthend/{month}/tasks/complete/employee` with `/monthend/{month}/my-tasks/complete`. Keep operation IDs, tags, summaries, responses and the employee endpoint's missing body. Remove `projectId` from `CompleteProjectLeadMonthEndTasksRequest` in `openapi/schemas/monthend.yaml` and keep `type` required. Verify with `mvn clean compile` that `completeProjectLeadMonthEndTasks(String month, UUID projectId, CompleteProjectLeadMonthEndTasksRequestDto)` and `completeEmployeeMonthEndTasks(String month)` are generated with the new `@Path` values, and that the request DTO has no `projectId`.

## 2. REST adapter

- [x] 2.1 In `MonthEndResource.completeProjectLeadMonthEndTasks`, take `projectId` from the new path parameter (via `transportHelper.toProjectId`) instead of the request body. Leave the use-case calls and response mapping unchanged. Verify with `MonthEndResourceTest`: move every existing bulk test (project-lead success, `400` for `EMPLOYEE_TIME_CHECK` and `ABRECHNUNG`, `403` for a non-led or unknown project, `200` with an empty list, role gates for both endpoints, bare employee POST without body or `Content-Type`) to the new paths and assert that the use case receives the path's `projectId`. Run `mvn test -Dtest=MonthEndResourceTest`.
- [x] 2.2 Update `MonthEndIT` bulk-completion calls to the new paths. Verify by running `mvn test -Dtest=MonthEndIT`.

## 3. Verification

- [x] 3.1 Verify with `grep -rn` over `src` that no references remain to `tasks/complete/project-lead` or `tasks/complete/employee`.
- [x] 3.2 Run `mvn test` and confirm the full suite, including `ArchitectureTest`, passes.
