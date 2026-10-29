# Tasks

## 1. ADMIN role

- [x] 1.1 Add `ADMIN` to the shared `Role` enum and the OpenAPI `Role` schema in `schemas/shared.yaml`. Verify `mvn compile` succeeds with the regenerated API model.
- [x] 1.2 Add an `AdminEmails` value object (normalised, case-insensitive `contains`) and an `AdminEmailsProducer` reading the optional `mega.admin.emails` list. Verify with unit tests for case and whitespace matching and for an absent list meaning no admins.
- [x] 1.3 Extend `UserRolePolicyService.determineRoles` to grant `ADMIN` on an admin-email match. Verify with unit tests for granting, for revoking when the email leaves the list, and that existing office-management and project-lead behaviour is unchanged.
- [x] 1.4 Add an empty or placeholder `mega.admin.emails` entry to `application.yaml`, plus a test-profile value. Verify the application starts in `%test` (for example, an existing `@QuarkusTest` passes).

## 2. Rule settings domain model

- [x] 2.1 Add the `WarningRule` enum (16 values, see spec) and add `WarningRule rule()` to `WorkTimeWarningCalculator`, implemented by every calculator. Verify with a unit test that the assembler's default calculators cover every `WarningRule` exactly once.
- [x] 2.2 Add the `WarningRuleSetting` record and the `WarningRuleSettings` collection with `isActive`, `appliesOn` and `missingRules`. Verify with unit tests for enabled/disabled, `activeFrom` before/on/after, a null `activeFrom`, and a missing rule treated as disabled.

## 3. Apply settings in warning evaluation

- [x] 3.1 Change `WorkTimeWarningAssembler.assemble` to take `WarningRuleSettings`, skip inactive rules, and drop dated warnings before `activeFrom`, while keeping the `EMPTY_ENTRY_LIST` shortcut. Verify with `WorkTimeWarningAssemblerTest` cases for a disabled rule, a start-date filter, a kept warning without a date, and an empty month with all rules disabled.
- [x] 3.2 Remove the hard-coded 2026-10-01 constants and their checks from the four calculators. Move their cutoff tests to the assembler level using a 2026-10-01 setting, and verify that `NonBillableOnBillableProjectCalculatorTest`, `InternalNoteConventionCalculatorTest`, `MealEntryCalculatorTest` and `TravelAllowanceCalculatorTest` pass.

## 4. Persistence

- [x] 4.1 Add the Liquibase changeset `016-worktime-warning-rule-settings.yaml` (table and one seed row per rule, with initial values as in the spec) and include it in `changelog-master.xml`. Verify that Liquibase runs in `@QuarkusTest` startup.
- [x] 4.2 Add the `WarningRuleSettingsPort` outbound port and its adapter (entity, Panache repository, MapStruct mapper) with `findAll` and `save`. Verify with a `@QuarkusTest` that saves and reloads a setting, including a null `activeFrom`.
- [x] 4.3 Add the seed-completeness `@QuarkusTest`. It asserts that `findAll()` returns a setting for every `WarningRule` and that the initial values match the spec. Verify it passes, and that it fails when a seed row is temporarily removed.

## 5. Warnings use case

- [x] 5.1 Make `GetEmployeeWarningsService` load `WarningRuleSettings` from the port, log `WARN` for `missingRules()`, and pass the settings to the assembler. Verify with unit tests that a disabled rule's warnings are absent, and that a missing setting logs and still returns the other warnings.

## 6. Admin REST API

- [x] 6.1 Add `GET /worktime/warning-rules` and `PUT /worktime/warning-rules/{rule}` with the `WarningRule`, `WarningRuleSetting` and `WarningRuleSettingUpdate` schemas to the OpenAPI contract. Verify `mvn compile` generates the API interface and models.
- [x] 6.2 Add the `ListWarningRuleSettingsUseCase` and `UpdateWarningRuleSettingUseCase` inbound ports and their `@ApplicationScoped @Transactional` services. Verify with unit tests using a mocked port.
- [x] 6.3 Add `WorkTimeWarningRuleResource` (`@MegaRolesAllowed(Role.ADMIN)`) and its REST mapper. Verify with REST-Assured tests: list returns all rules, update persists and returns the entry, unknown rule → 404, missing `enabled` → 400, non-admin → 403.
- [x] 6.4 Add `http-client/` requests for listing and updating rule settings. Verify they run against `mvn quarkus:dev`.

## 7. Integration check

- [x] 7.1 End-to-end `@QuarkusTest`: request an employee's warnings with a `MEAL_ENTRY_MISSING` warning present, disable `MEAL_ENTRY` through the admin endpoint, request again, and assert the warning is gone. Verify the full `mvn test` suite passes, including `ArchitectureTest`.

## Workflow follow-up

- Configure the two admin email addresses in the deployment config, then trigger or wait for the user sync.
