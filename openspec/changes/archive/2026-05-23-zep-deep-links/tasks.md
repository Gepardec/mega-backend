## 1. Extend ZepConfig with URL builders

- [x] 1.1 Add `buildProjectUrl(int zepId): String` to `ZepConfig` — combines `origin` with the hardcoded project sub-path
- [x] 1.2 Add `buildEmployeeUrl(ZepUsername username): String` to `ZepConfig` — combines `origin` with the hardcoded employee sub-path

## 2. Extend the shared OpenAPI schema

- [x] 2.1 Add `zepUrl: string` (nullable, not in `required`) to the `ProjectRef` schema in `src/main/resources/openapi/schemas/shared.yaml`
- [x] 2.2 Add `zepUrl: string` (nullable, not in `required`) to the `UserRef` schema in `src/main/resources/openapi/schemas/shared.yaml`
- [x] 2.3 Regenerate DTOs (run `mvn generate-sources`) and verify `ProjectRefDto` and `UserRefDto` contain the new `zepUrl` field

## 3. Update MonthEndRestMapper

- [x] 3.1 Convert `MonthEndRestMapper` from an interface to an abstract class
- [x] 3.2 Inject `ZepConfig` via `@Inject` in the abstract class
- [x] 3.3 Implement `toDto(ProjectRef)` manually — maps `id`, `name`, and sets `zepUrl` via `zepConfig.buildProjectUrl(project.zepId())`
- [x] 3.4 Implement `toDto(UserRef)` manually — maps `id`, `fullName`, and sets `zepUrl` to `zepConfig.buildEmployeeUrl(user.zepUsername())` when `zepUsername` is non-null, else `null`

## 4. Test

- [x] 4.1 Unit-test `ZepConfig.buildProjectUrl` — verify the assembled URL matches the expected format for a sample `zepId`
- [x] 4.2 Unit-test `ZepConfig.buildEmployeeUrl` — verify the assembled URL matches the expected format for a sample `ZepUsername`
- [x] 4.3 Unit-test `MonthEndRestMapper.toDto(ProjectRef)` — verify `zepUrl` is correctly assembled
- [x] 4.4 Unit-test `MonthEndRestMapper.toDto(UserRef)` — verify `zepUrl` is set when `zepUsername` is present and `null` when absent
- [x] 4.5 Run `mvn test` and confirm all existing tests still pass
