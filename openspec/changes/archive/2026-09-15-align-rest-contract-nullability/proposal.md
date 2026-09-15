## Why

The backend always serializes absent values as explicit JSON `null` — it never omits a key — but the hand-authored OpenAPI contract marks those fields `nullable` without listing them in `required`. Generated clients therefore type them as optional (`prop?: T | null`) instead of always-present-and-nullable (`prop: T | null`), which forced the Angular frontend to re-declare seven response types and normalize every value with `?? null`. Several `$ref` properties additionally used the OAS 3.0 `$ref` + `nullable` sibling form, which the bundler silently discards, so fields the contract author intended to be nullable reached clients with no `null` in their type at all.

## What Changes

- Every response property that the backend always sends is now `required`, with `nullable: true` where the value can be `null`: `UserRef.zepUrl`, `ProjectRef.zepUrl`, `User.releaseDate`, `User.personioId`, `ActiveUser.releaseDate`, `ApiError.message`, `MonthEndStatusOverviewEntry.{subjectEmployee, completedBy}`, `MonthEndOverviewClarificationEntry.{subjectEmployee, resolutionNote, resolvedBy, resolvedAt}`, `MonthEndTask.{subjectEmployeeId, completedBy}`, `WorkTimeWarning.{date, hours}`.
- Nullable `$ref` properties move from the discarded sibling form to `allOf` + `nullable`, so their nullability survives bundling.
- `Role` becomes a named enum schema (`EMPLOYEE`, `OFFICE_MANAGEMENT`, `PROJECT_LEAD`, `SYSTEM`) mirroring the shared domain `Role` enum, and `User.roles` references it instead of an unconstrained `string` array.
- The internal-rates CSV template response declares `format: binary`, so clients receive a binary body rather than a decoded string.
- Request-body properties are deliberately left optional: `CreateClarificationRequest.subjectEmployeeId` and `ResolveClarificationRequest.resolutionNote` stay omittable.

No endpoint, status code, or field name changes. The wire format is unchanged — this aligns the contract with what the service already sends.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `worktime-rest-api`: the warnings endpoint requirement describes each warning as carrying "an optional `date`, and an optional `hours` value". Both fields are now always present and nullable.
- `user-rest-api`: the `GET /users/me` requirement describes `roles` as an "array of role strings". The contract now constrains it to a closed set of role values.
- `monthend-status-overview`: the scenario for an `OPEN` task states the overview entry "does not include a completing actor". The entry now always carries a completing actor field whose value is `null` for open tasks.

Specs reviewed and deliberately **not** modified, because their wording already matches the new contract: `shared-user-project-refs` (already requires `zepUrl` to be `null` when unavailable), `monthend-rest-api` and `monthend-clarifications` (already describe nullable nested employee references and a nullable resolution note), `worktime-warnings` and `user-aggregate` (domain-level requirements, unaffected by the REST representation), and `system-actor` (its `Role.SYSTEM` requirement is about the shared domain enum, which is unchanged).

## Impact

- **Contract**: `src/main/resources/openapi/schemas/{shared,user,monthend,worktime}.yaml` and `src/main/resources/openapi/paths/user.yaml`.
- **Generated backend model**: required properties gain `@NotNull` on their getters. This is inert for response DTOs — every generated resource method returns a raw `Response` and no return type is `@Valid`-annotated, so outbound DTOs are never bean-validated. It is the reason request-body properties were left optional.
- **`UserDto.roles`** becomes `List<RoleDto>`; MapStruct maps the domain `Role` enum to it by name with no mapper change. Two test assertions comparing role strings were updated to `RoleDto` constants.
- **Frontend consumers**: the generated Angular client loses the optional markers, letting `monthend-api.model.ts`, `monthend-api.mapper.ts`, `active-user.ts`, `work-time-booking-warning.ts` and the `?? null` normalizers be deleted downstream. The CSV template operation returns `Blob` instead of `string`.
