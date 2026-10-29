# Design

## Context

- `WorkTimeWarningAssembler` (domain) builds 16 calculators with `new` and runs all of them for every request. `GetEmployeeWarningsService` creates the assembler itself. It is not a CDI bean.
- `NonBillableOnBillableProjectCalculator`, `InternalNoteConventionCalculator`, `MealEntryCalculator` and `TravelAllowanceCalculator` each hold a private `LocalDate.of(2026, 10, 1)` and skip anything dated earlier. In all four, the cutoff only limits which dated warnings are *emitted*. Month-wide inputs are still computed from all bookings, for example `containsBillableWork` in the internal-note rule and trip derivation in the meal-entry rule.
- Warnings are calculated per request and never stored.
- The worktime BC currently has only ZEP and snapshot outbound adapters, no tables of its own.
- Roles live on the `User` aggregate (`user_roles` table, stored as enum strings, no check constraint). `UserRolePolicyService.determineRoles` assigns them on every sync from email lists. `AuthenticatedActorContext` reads the roles from the stored user, and `@MegaRolesAllowed` enforces them.
- The OpenAPI `Role` schema in `schemas/shared.yaml` mirrors the domain `Role` enum.

## Goals / Non-Goals

**Goals:**
- One place decides whether a rule applies on a date. Calculators no longer contain cutoff logic.
- Warnings stay identical after the first deployment.
- The settings stay behind a port, so the database storage could later be replaced by a flag service without touching the domain.

**Non-Goals:**
- Per-employee or per-project targeting, percentage rollouts.
- An audit trail or change history.
- A frontend screen.
- Settings for the `EMPTY_ENTRY_LIST` shortcut in the assembler.

## Decisions

### 1. Rule identity: a `WarningRule` enum, and each calculator declares its rule
`WorkTimeWarningCalculator` gains `WarningRule rule()`. The enum values mirror the existing `worktime-warning-*` spec names (see the spec for the list). This keeps the calculator, the spec and the setting easy to match up.

*Alternative:* key the settings by `WorkTimeWarningType`. Rejected: `INTERNAL_NOTE` and `TRAVEL_ALLOWANCE` each emit two types, which one switch must cover.

### 2. The assembler applies the settings by filtering what calculators return
`assemble(evaluation, settings)`:
1. Skip calculators whose rule is disabled or has no setting.
2. Drop warnings whose `date` is before the rule's `activeFrom`. Warnings without a date are kept.

The four hard-coded constants and their `isBefore` checks are removed.

Because each existing cutoff only suppresses emitted warnings (see Context), filtering afterwards gives exactly the same results. It also gives the 12 legacy calculators a start date for free.

*Alternative:* pass `activeFrom` into each calculator, either in `WorkTimeEvaluation` or as a parameter. Rejected: it repeats the same filter in 16 places, and the logic belongs to the rule settings rather than to any one rule.

### 3. Domain model: `WarningRuleSetting` and `WarningRuleSettings`
- `WarningRuleSetting(rule, enabled, activeFrom)` is a record. `activeFrom` may be null.
- `WarningRuleSettings` is a collection keyed by rule. `isActive(rule)` and `appliesOn(rule, date)` treat a missing rule as disabled, and `missingRules()` lists the rules without a setting.
- Updating a setting replaces the record in full. There are no invariants beyond non-null `rule` and `enabled`, so no aggregate behaviour is needed.

### 4. Persistence: one table, primary key = rule name
- Table `worktime_warning_rule_setting(rule varchar PK, enabled boolean not null, active_from date null)`.
- A new Liquibase changeset `016-worktime-warning-rule-settings.yaml` creates the table and inserts one row per rule (see the spec for the initial values).
- `WarningRuleSettingsPort` (outbound, application layer) provides `findAll()` and `save(setting)`. The adapter is a Panache repository plus a MapStruct mapper, following the existing outbound adapters.

*Alternative:* store the settings as a JSON blob or a generic key-value table. Rejected: a typed `date` column validates on write and keeps the queries simple.

### 5. Missing row: off plus a `WARN`, caught at build time
- `GetEmployeeWarningsService` loads `WarningRuleSettings` once per request. If `missingRules()` is not empty, it logs `Log.warnf("Warning rules without setting, treated as disabled: %s", …)`. Logging stays in the application layer, so the domain stays free of frameworks.
- A `@QuarkusTest` against the real changelog on H2 checks that `findAll()` covers every `WarningRule`. A new enum value without a seed row fails the build.

*Alternative:* fail the warnings request. Rejected: one unconfigured rule would take down the whole warnings view.

### 6. No caching
One indexed read of about 16 rows per warnings request is negligible next to the ZEP calls in the same request. Without a cache, an update takes effect immediately and there is no invalidation to get wrong.

### 7. REST: OpenAPI first, with framework defaults for errors
- Add `/worktime/warning-rules` and `/worktime/warning-rules/{rule}` to `paths/worktime.yaml`. Add `WarningRule` (enum), `WarningRuleSetting` (response) and `WarningRuleSettingUpdate` (request: `enabled` required, `activeFrom` nullable `format: date`) to `schemas/worktime.yaml`.
- `WorkTimeWarningRuleResource` implements the generated interface with `@MegaRolesAllowed(Role.ADMIN)`. It calls new inbound use cases, `ListWarningRuleSettingsUseCase` and `UpdateWarningRuleSettingUseCase`, implemented by `@ApplicationScoped @Transactional` services.
- An unknown `{rule}` cannot be converted to the enum path parameter, and JAX-RS answers that with 404 by default. A missing `enabled` fails Bean Validation on the generated model and gets the default 400. No custom error handling.

### 8. `ADMIN` role mirrors the office-management mechanism
- Add `Role.ADMIN` to the shared kernel enum and to the OpenAPI `Role` schema.
- Add an `AdminEmails` value object in `user.domain.model` with the same normalising `contains` as `OfficeManagementEmails`, and an `AdminEmailsProducer` reading `mega.admin.emails`. It is an `Optional<List<String>>`, so an absent list means no admins.
- `UserRolePolicyService.determineRoles` adds `ADMIN` on a match. Roles are recalculated from scratch on each sync, so removing an email revokes the role at the next sync.

*Alternative:* a Keycloak realm role. Rejected: the app's authorization reads roles from the stored `User`, not from the token, so a second source of roles would be inconsistent.

## Risks / Trade-offs

- [A change to the settings changes past months' warnings] → This is intended (see the spec). Changing a start date is a deliberate admin action.
- [Admin access depends on the next user sync after a config change] → Acceptable for two rarely changing users. Trigger the sync manually if it's urgent.
- [Anyone with `ADMIN` can silence any rule] → Only two users get it, and the list is in reviewed config. No audit trail, as decided.
- [The post-filter assumes cutoffs never change month-wide calculations] → This holds for all four rules today. A future rule that needs a start-dependent aggregate should take `activeFrom` explicitly. Decision 2 notes the limitation.

## Migration Plan

1. Deploy. Liquibase creates the table and seeds the rows, and behaviour is unchanged.
2. Add the two admin emails to `mega.admin.emails` in the deployment config and wait for or trigger the user sync.
3. Rollback: redeploying the previous version ignores the table, so leaving it in place does no harm. An unrecognised `ADMIN` role row would fail to load in the old version's enum mapping, so remove `ADMIN` from `user_roles` before a rollback.

## Open Questions

- The two admin email addresses are a deployment config value and can be supplied at rollout.
