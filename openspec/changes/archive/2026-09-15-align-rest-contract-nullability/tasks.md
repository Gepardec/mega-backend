> Implementation landed in the working tree before this change was written up, so the
> boxes below record work already completed and verified rather than work still to do.

## 1. Shared and user contract schemas

- [x] 1.1 In `schemas/shared.yaml`, add `zepUrl` to the `required` list of `UserRef` and `ProjectRef`; verify the bundled spec at `target/generated-sources/openapi-bundle/openapi/openapi.yaml` lists `zepUrl` under each schema's `required` while keeping `nullable: true`
- [x] 1.2 In `schemas/shared.yaml`, give `ApiError` a `required: [message]` list and mark `message` `nullable: true`; verify the bundled `ApiError` carries both
- [x] 1.3 In `schemas/shared.yaml`, add a `Role` enum schema with `EMPLOYEE`, `OFFICE_MANAGEMENT`, `PROJECT_LEAD`, `SYSTEM`, and point `User.roles.items` in `schemas/user.yaml` at it; verify `mvn -o compile` generates `RoleDto` and types `UserDto.roles` as `List<RoleDto>`
- [x] 1.4 In `schemas/user.yaml`, add `releaseDate` and `personioId` to `User.required` and `releaseDate` to `ActiveUser.required`; verify the bundled schemas list them

## 2. Month-end contract schemas

- [x] 2.1 In `schemas/monthend.yaml`, convert the `$ref` + `nullable` siblings on `MonthEndStatusOverviewEntry.subjectEmployee`, `MonthEndOverviewClarificationEntry.subjectEmployee` and `MonthEndOverviewClarificationEntry.resolvedBy` to the `allOf` + `nullable` form; verify `nullable: true` survives into the bundled spec for all three
- [x] 2.2 Add `subjectEmployee` and `completedBy` to `MonthEndStatusOverviewEntry.required`; verify against the bundled spec
- [x] 2.3 Add `subjectEmployee`, `resolutionNote`, `resolvedBy` and `resolvedAt` to `MonthEndOverviewClarificationEntry.required`; verify against the bundled spec
- [x] 2.4 Add `subjectEmployeeId` and `completedBy` to `MonthEndTask.required`; verify against the bundled spec
- [x] 2.5 Confirm `CreateClarificationRequest.subjectEmployeeId` and `ResolveClarificationRequest.resolutionNote` are left untouched; verify the generated `CreateClarificationRequestDto` and `ResolveClarificationRequestDto` getters carry no `@NotNull`

## 3. Work time contract schema

- [x] 3.1 In `schemas/worktime.yaml`, mark `WorkTimeWarning.date` and `WorkTimeWarning.hours` `nullable: true` and add both to `required`; verify the bundled `WorkTimeWarning` lists `date`, `hours` and `type` as required

## 4. CSV template response

- [x] 4.1 In `paths/user.yaml`, add `format: binary` to the `text/csv` response schema of `getInternalRatesCsvTemplate`; verify the generated `UserApi.getInternalRatesCsvTemplate` still returns a raw `Response` so `UserResource` needs no change

## 5. Verification

- [x] 5.1 Update the two test assertions that compare role strings (`UserResourceTest`, `UserRestMapperTest`) to `RoleDto` constants; verify `mvn -o test-compile` succeeds
- [x] 5.2 Run `mvn -o test` and verify the full suite passes with no new failures (887 tests, 0 failures, 7 pre-existing skips)
- [x] 5.3 Verify no response payload changed by confirming the existing REST resource tests pass unmodified apart from the two role assertions in 5.1
