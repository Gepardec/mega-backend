## REMOVED Requirements

### Requirement: Bookings on unknown projects are not checked
**Reason**: The `BILLABLE_PROJECT` warning rule is dropped completely (team decision 2026-11-17). Non-billable bookings on billable projects are legitimate, e.g. on the per-project task "Reisepauschale", and would be reported as false positives.
**Migration**: None. No `NON_BILLABLE_ON_BILLABLE_PROJECT` warnings are produced anymore; clients must not expect this type.

### Requirement: The project's current billability is decisive
**Reason**: The `BILLABLE_PROJECT` warning rule is dropped completely (team decision 2026-11-17).
**Migration**: None. Project billability remains an input of the `INTERNAL_NOTE` and `TRAVEL_ALLOWANCE` rules, which define their own billability semantics.

### Requirement: Billable-project bookings are only checked from the rule's configured start date
**Reason**: The `BILLABLE_PROJECT` warning rule is dropped completely (team decision 2026-11-17), so it has no rule setting anymore.
**Migration**: The `BILLABLE_PROJECT` rule setting is deleted on upgrade; `GET /worktime/warning-rules` no longer lists it and `PUT /worktime/warning-rules/BILLABLE_PROJECT` is rejected with 404.

### Requirement: Non-billable bookings on billable projects produce one warning per day
**Reason**: The `BILLABLE_PROJECT` warning rule is dropped completely (team decision 2026-11-17). Correct bookings on the non-billable task "Reisepauschale" on billable customer projects would be flagged.
**Migration**: None. The frontend can drop the translation key for `NON_BILLABLE_ON_BILLABLE_PROJECT`.
