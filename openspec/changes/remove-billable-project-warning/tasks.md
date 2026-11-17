# Tasks

## 1. Delete the persisted rule setting

- [ ] 1.1 Add `src/main/resources/db/changelog/hexagon/017-remove-billable-project-warning-rule.yaml` with a new changeset (do not touch 016) that deletes the row `rule = 'BILLABLE_PROJECT'` from `worktime_warning_rule_setting`, and include it after 016 in `src/main/resources/db/changelog-master.xml`; verify the file is included and Liquibase applies it in `mvn test -Dtest=WarningRuleSettingsSeedTest` (passes once group 2 lands, see 2.5).

## 2. Remove the rule, its warning type and calculator (ship together with group 1)

- [ ] 2.1 Delete `worktime/domain/services/warning/NonBillableOnBillableProjectCalculator.java` and its registration in `WorkTimeWarningEvaluator` (~line 32); delete `NonBillableOnBillableProjectCalculatorTest`; verify the project compiles.
- [ ] 2.2 Remove `BILLABLE_PROJECT` from `WarningRule` and `NON_BILLABLE_ON_BILLABLE_PROJECT` from `WorkTimeWarningType`; remove `BILLABLE_PROJECT` from the `WarningRule` enum in `src/main/resources/openapi/schemas/worktime.yaml` in the same step so the generated `WarningRuleDto` and `WarningRuleSettingRestMapper` stay in sync; verify `mvn compile` succeeds.
- [ ] 2.3 In `GetEmployeeWarningsService.billableProjectZepIds`, drop `WarningRule.BILLABLE_PROJECT` from the guard so billable project ids are still loaded when `INTERNAL_NOTE` or `TRAVEL_ALLOWANCE` is active; keep `WorkTimeEvaluation.billableProjectZepIds` and the project snapshot lookup unchanged.
- [ ] 2.4 Update `WorkTimeWarningEvaluatorTest`: remove `BILLABLE_PROJECT` from the `dated` set in `initialSettings()`; delete `evaluate_whenBillableProjectRuleStartsAfterBooking_thenNoWarning_andOnStartDayWarning` with its helper `billableProjectWarnings`; rewrite `evaluate_whenNonBillableBookingWithoutMarkerIsOnBillableProjectFromOctober2026_...` to assert that a non-billable unmarked booking on a billable project yields neither `INTERNAL_NOTE_MISSING` nor any other booking-convention warning (the "Reisepauschale" case); verify `mvn test -Dtest=WorkTimeWarningEvaluatorTest` passes, including `defaultCalculators_coverEveryWarningRuleExactlyOnce`.
- [ ] 2.5 Update `WarningRuleSettingsSeedTest`: remove `BILLABLE_PROJECT` from the dated-rule condition; verify `mvn test -Dtest=WarningRuleSettingsSeedTest` passes (the table holds exactly one row per remaining rule, proving changeset 017 deleted the row).
- [ ] 2.6 Update `WarningRuleSettingsAdapterTest.save_shouldPersistNullActiveFrom` to use `WarningRule.INTERNAL_NOTE`; verify `mvn test -Dtest=WarningRuleSettingsAdapterTest` passes.
- [ ] 2.7 Update `GetEmployeeWarningsServiceTest`: remove `BILLABLE_PROJECT` from the `dated` set in `settings(...)` and from the disabled rules in `getWarnings_rulesNeedingBillabilityDisabled_skipsBillableProjectLookup`; repoint `getWarnings_passesBillableProjectIdsOfBookedProjectsToEvaluator` to a remaining billability-dependent outcome (e.g. `TRAVEL_ALLOWANCE_WRONG_PROJECT` for a passive journey on a billable project with travel allowance, or verify `findBillableZepIds(Set.of(42))` is called); in `getWarnings_bookingOnUnknownProjectDoesNotFailTheRequest` assert the request succeeds without referencing the removed type; verify `mvn test -Dtest=GetEmployeeWarningsServiceTest` passes.
- [ ] 2.8 Update `WorkTimeResourceTest`: delete `getEmployeeWarnings_shouldReturnNonBillableOnBillableProjectWarningWithTypeDateAndNullHours` (the type/date/null-hours shape is already covered by the `INTERNAL_NOTE_BILLABLE` test); verify `mvn test -Dtest=WorkTimeResourceTest` passes.
- [ ] 2.9 Update `WorkTimeWarningRuleResourceTest`: switch `update_shouldRemoveStartDateWhenNull` to `PUT /worktime/warning-rules/INTERNAL_NOTE`; add a test that `PUT /worktime/warning-rules/BILLABLE_PROJECT` returns 404 and that `GET /worktime/warning-rules` contains no `BILLABLE_PROJECT` entry; verify `mvn test -Dtest=WorkTimeWarningRuleResourceTest` passes.
- [ ] 2.10 Search `src/` for remaining `BILLABLE_PROJECT` / `NON_BILLABLE_ON_BILLABLE_PROJECT` / `NonBillableOnBillableProject` references; verify the only hits are changeset 016, changeset 017 and unrelated local test constants (`InternalNoteConventionCalculatorTest`, `TravelAllowanceCalculatorTest`).

## 3. Integration checks

- [ ] 3.1 Run `mvn test` and verify the full suite passes, including `HexagonalArchitectureTest`.
- [ ] 3.2 Run the code hygiene check `mvn package -Prewrite-check`; if it reports drift, fix it with `mvn rewrite:run` and re-run the check until the build passes.
