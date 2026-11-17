# Proposal

## Why

Project leads can now create a non-billable task "Reisepauschale" on (usually billable) customer projects, so that travel allowance is tracked per project. Correct bookings on that task are non-billable bookings on a billable project and would be flagged by the `BILLABLE_PROJECT` rule as false positives. The team decided on 2026-11-17 to drop this warning rule completely rather than exempt the task.

## What Changes

- **BREAKING** Remove the `BILLABLE_PROJECT` warning rule. The warnings evaluation no longer produces `NON_BILLABLE_ON_BILLABLE_PROJECT` warnings for any month, including past months.
- **BREAKING** Remove `BILLABLE_PROJECT` from the warning-rule vocabulary of the REST contract: `GET /worktime/warning-rules` no longer lists it and `PUT /worktime/warning-rules/BILLABLE_PROJECT` is rejected as an unknown rule (404).
- **BREAKING** Remove `NON_BILLABLE_ON_BILLABLE_PROJECT` from the warning-type vocabulary.
- Delete the persisted setting of the `BILLABLE_PROJECT` rule with a new database migration that ships together with the removal, so existing deployments keep exactly one setting per remaining rule.
- Project billability stays an input of the warnings evaluation, because the `INTERNAL_NOTE` and `TRAVEL_ALLOWANCE` rules still depend on it. Their behaviour is unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `worktime-warning-billable-project`: removed entirely; all of its requirements are dropped.
- `worktime-warnings`: the warning-type vocabulary loses `NON_BILLABLE_ON_BILLABLE_PROJECT`; the billability example scenario and the billable-project identifier scenario are replaced or dropped.
- `worktime-warning-rule-settings`: the rule vocabulary loses `BILLABLE_PROJECT`; the initial seed no longer contains it; scenarios that used `BILLABLE_PROJECT` as an example switch to `INTERNAL_NOTE`.

Reviewed and unaffected: `worktime-warning-internal-note` (a non-billable booking on a billable project already does not trigger `INTERNAL_NOTE_MISSING`; that stays as is, so such bookings now produce no warning at all, which is the intended outcome for "Reisepauschale"), `worktime-warning-travel-allowance`, `worktime-rest-api` and the remaining `worktime-warning-*` specs.

## Impact

- Domain: the billable-project warning calculator, the `BILLABLE_PROJECT` rule identifier and the `NON_BILLABLE_ON_BILLABLE_PROJECT` warning type in the `worktime` BC.
- Application: the billability lookup in the warnings service is kept, but no longer triggered by the removed rule.
- Persistence: new Liquibase changeset deleting the `BILLABLE_PROJECT` row from the warning-rule settings table.
- API: OpenAPI `WarningRule` enum loses `BILLABLE_PROJECT`. The warning `type` field is a free string in the contract, so only its documented value set changes.
- Frontend (separate repo, no work here): can drop the translation key for `NON_BILLABLE_ON_BILLABLE_PROJECT` and must tolerate the rule disappearing from the rule-settings list and the type disappearing from warnings responses.
- Tests in the `worktime` BC that cover or use the removed rule as an example.
