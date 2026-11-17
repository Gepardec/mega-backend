# Design

## Context

See proposal.md (Why). Current state in the `worktime` BC (`com.gepardec.mega.hexagon.worktime`):

- `NonBillableOnBillableProjectCalculator` (domain, `domain/services/warning/`) produces `WorkTimeWarningType.NON_BILLABLE_ON_BILLABLE_PROJECT` and is governed by `WarningRule.BILLABLE_PROJECT`. It is registered in the default calculator list of `WorkTimeWarningEvaluator`.
- `WorkTimeWarningEvaluatorTest.defaultCalculators_coverEveryWarningRuleExactlyOnce` asserts that the registered calculators cover exactly `WarningRule.values()`, so the enum value and the calculator must go together.
- `GetEmployeeWarningsService.billableProjectZepIds(...)` loads billable project ZEP ids only when `BILLABLE_PROJECT`, `INTERNAL_NOTE` or `TRAVEL_ALLOWANCE` is active. `InternalNoteConventionCalculator` and `TravelAllowanceCalculator` still consume `WorkTimeEvaluation.billableProjectZepIds`.
- Rule settings are persisted in `worktime_warning_rule_setting` (seeded by changeset `hexagon-worktime-warning-rule-settings-001` in `db/changelog/hexagon/016-worktime-warning-rule-settings.yaml`, which inserts a `BILLABLE_PROJECT` row). `WarningRuleSettingEntity.rule` is `@Enumerated(EnumType.STRING)` and is the primary key.
- OpenAPI: `src/main/resources/openapi/schemas/worktime.yaml` lists `BILLABLE_PROJECT` in the `WarningRule` enum. `WorkTimeWarning.type` is a plain string without enum, so no contract enum exists for warning types.
- The `{rule}` path parameter of `PUT /worktime/warning-rules/{rule}` is bound to the `WarningRuleDto` enum generated from the OpenAPI `WarningRule` schema; an unknown value already yields 404 (`WorkTimeWarningRuleResourceTest.update_shouldRejectUnknownRuleWith404`). After the removal, `BILLABLE_PROJECT` falls into that path without extra code.
- `WarningRuleSettingRestMapper` (MapStruct) maps `WarningRuleDto` ↔ `WarningRule` by constant name. MapStruct fails the build on an unmapped enum constant, so the OpenAPI enum value and the domain enum value must be removed in the same step.

## Goals / Non-Goals

**Goals:**
- Remove the rule, its warning type, its calculator and its persisted setting in one deployable unit.
- Keep the billability lookup for the `INTERNAL_NOTE` and `TRAVEL_ALLOWANCE` rules intact.

**Non-Goals:**
- Changing `INTERNAL_NOTE_MISSING` semantics. A non-billable booking on a billable project already does not trigger it; that stays.
- Any exemption logic for the "Reisepauschale" task. The rule is removed, not refined.
- Frontend changes (separate repo).
- Rewriting archived changes or the existing changeset 016.

## Decisions

### D1: Delete the setting row with a new Liquibase changeset, shipped with the enum removal

Add `src/main/resources/db/changelog/hexagon/017-remove-billable-project-warning-rule.yaml` with one changeset (e.g. id `hexagon-remove-billable-project-warning-rule-001`) containing a `delete` on `worktime_warning_rule_setting` where `rule = 'BILLABLE_PROJECT'`, and include it in `db/changelog-master.xml` after 016.

Why it is mandatory: with the enum value gone but the row still present, Hibernate fails to map the `rule` column when `WarningRuleSettingsAdapter.findAll()` loads all rows. That would break both `GET /worktime/employees/.../warnings` (via `GetEmployeeWarningsService.loadRuleSettings`) and `GET /worktime/warning-rules`. The changeset and the enum removal must therefore be in the same release.

Alternatives considered:
- *Edit changeset 016 to drop the insert*: rejected. 016 has already run on existing databases; editing it changes its checksum and Liquibase refuses to start (and existing rows would still remain).
- *Keep the enum value as deprecated and ignore it*: rejected. It would keep the rule in the REST vocabulary and the coverage test, contradicting the decision to drop it completely.
- *Make the adapter tolerate unknown rule values*: rejected as unnecessary plumbing; the migration solves the problem at its source.

No rollback changeset content is required beyond Liquibase's default; rolling back the application version would need the row re-inserted (see Migration Plan).

### D2: Remove domain artefacts together

Delete `NonBillableOnBillableProjectCalculator`, its registration in `WorkTimeWarningEvaluator`, `WarningRule.BILLABLE_PROJECT`, `WorkTimeWarningType.NON_BILLABLE_ON_BILLABLE_PROJECT` and the `BILLABLE_PROJECT` value of the OpenAPI `WarningRule` enum in one step, so the evaluator's coverage invariant and the MapStruct enum mapping hold at every commit.

### D3: Keep the billability lookup, drop only the removed rule from its trigger

In `GetEmployeeWarningsService.billableProjectZepIds`, the guard becomes `INTERNAL_NOTE` or `TRAVEL_ALLOWANCE`. `WorkTimeEvaluation.billableProjectZepIds` and `ProjectSnapshotPort.findBillableZepIds` stay unchanged.

### D4: Tests that used `BILLABLE_PROJECT` only as an example switch to `INTERNAL_NOTE`

`INTERNAL_NOTE` is also seeded with `activeFrom` 2026-10-01, so dated-rule examples keep their meaning. Tests whose purpose was the removed behaviour are deleted or repointed to a remaining billability-dependent rule (`TRAVEL_ALLOWANCE_WRONG_PROJECT` or `INTERNAL_NOTE_MISSING`), see tasks.md for the per-test mapping.

### D5: Spec delta for the warning-type vocabulary keeps one scenario name

OpenSpec's MODIFIED check refuses to drop an existing scenario by name. The scenario "The billable-project rule emits its own identifier" in `worktime-warnings` is therefore kept by name but now asserts that `NON_BILLABLE_ON_BILLABLE_PROJECT` is no longer produced. The name can be tidied directly in the main spec after archive if desired.

## Risks / Trade-offs

- [Enum removed but row still present, e.g. changeset not included in master] → Warnings and rule-settings endpoints fail with 500. Mitigation: `WarningRuleSettingsSeedTest` runs against the full changelog in H2 and asserts the table holds exactly one row per `WarningRule` value; it would fail if the row survived (`WarningRule.valueOf` throws).
- [Frontend still sends `PUT /worktime/warning-rules/BILLABLE_PROJECT` or expects the rule in the list] → 404 / missing entry. Mitigation: called out in proposal; the frontend renders the list it receives.
- [Frontend has no translation for a removed type] → none, the type simply no longer appears.
- [Admin-customised `BILLABLE_PROJECT` setting is lost] → Accepted; the rule no longer exists.

## Migration Plan

1. Deploy the release containing the code removal and changeset 017 together. Liquibase deletes the row on startup before the application serves requests.
2. Rollback to the previous application version: re-insert the `BILLABLE_PROJECT` row (enabled, `active_from` 2026-10-01) manually, otherwise the previous version treats the rule as missing (disabled, logged as warning) — which is harmless, so a manual re-insert is optional.
