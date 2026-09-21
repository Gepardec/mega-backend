# Tasks

## 1. Contract

- [x] 1.1 In `src/main/resources/openapi/openapi.yaml`'s referenced `paths/projects.yaml`, replace the toggle's `'204': { description: Flag updated }` with a `'200'` response carrying `ProjectSettings` as `application/json`; leave the `400`, `403` and `404` responses untouched, and verify `mvn -q compile` regenerates `ProjectApi` without contract errors

## 2. Application layer

- [x] 2.1 Change `SetLeistungsnachweisEnabledUseCase.setLeistungsnachweisEnabled` to return `Project` instead of `void`, and verify the module still compiles
- [x] 2.2 Return the saved aggregate from `SetLeistungsnachweisEnabledService` (it already holds it as `updated`), keeping the existing not-found, forbidden, domain-rule and logging behaviour unchanged

## 3. REST adapter

- [x] 3.1 In `ProjectResource.setLeistungsnachweisEnabled`, map the returned project with `projectRestMapper.toDto` and answer `Response.ok(dto)` instead of `Response.noContent()`

## 4. Tests

- [x] 4.1 Update `SetLeistungsnachweisEnabledServiceTest` so the success cases assert the returned aggregate carries the new flag value, and verify the test class passes
- [x] 4.2 Update `ProjectResourceTest` so the two success cases (enable, disable) assert `200` and a body whose `leistungsnachweisEnabled` matches the persisted value and whose `project.id`, `project.name` and `project.zepUrl` are present; keep the `400`, `403` and `404` cases as they are
- [x] 4.3 Add a REST case for disabling on a non-billable project, asserting `200` with `leistungsnachweisEnabled=false`, so the spec scenario has a test

## 5. Verification

- [x] 5.1 Run `mvn -q test` (or at least the project BC test classes) and verify the suite passes
- [x] 5.2 Run `openspec validate return-project-settings-on-toggle --strict` and verify the change is valid
