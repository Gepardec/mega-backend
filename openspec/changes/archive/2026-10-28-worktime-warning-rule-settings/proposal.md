# Proposal

## Why

The four new warning rules (billable project, internal note, meal entry, travel allowance) each hard-code 2026-10-01 as their start date, and every warning rule is always active. Moving a start date or switching a rule off currently needs a code change and a redeploy. Administrators need to adjust both at runtime, without deploying.

## What Changes

- Introduce **warning rule settings**: one persisted setting per warning rule with an `enabled` switch and an optional `activeFrom` date. Every rule has exactly one setting.
- Warning evaluation applies the settings on every request: a disabled rule produces no warnings, and an enabled rule with `activeFrom` produces no warnings dated before that date. Changes take effect on the next warnings request, including for past months.
- Seed initial settings that preserve today's behaviour: the four new rules are enabled from 2026-10-01, and all other rules are enabled without a start date.
- A rule without a setting is treated as disabled and logged as a warning. This is a safety net; a build-time check ensures every rule has a seeded setting.
- Add administration endpoints to list all rule settings and update one rule's setting.
- Introduce a new `ADMIN` role, assigned during user sync from a configured email list (the same mechanism as office management). Only `ADMIN` may read or change rule settings.
- Remove the hard-coded 2026-10-01 cutoffs from the four rules. Their start date now comes from the rule settings.

## Capabilities

### New Capabilities
- `worktime-warning-rule-settings`: per-rule activation settings (enabled, active-from date), how they are applied during warning evaluation, their initial values, and the admin endpoints to list and update them.

### Modified Capabilities
- `worktime-warning-billable-project`: the fixed 2026-10-01 cutoff becomes the rule's configured start date.
- `worktime-warning-internal-note`: the fixed 2026-10-01 cutoff becomes the rule's configured start date.
- `worktime-warning-meal-entry`: the fixed 2026-10-01 cutoff becomes the rule's configured start date. The "only past trip days" part is unchanged.
- `worktime-warning-travel-allowance`: the fixed 2026-10-01 cutoff becomes the rule's configured start date.
- `user-aggregate`: the role vocabulary gains `ADMIN`, assigned from a configured admin email list.
- `user-role-policy`: role determination during sync also grants `ADMIN` for configured admin emails.

## Impact

- **Worktime BC**: the warning assembler and the four calculators, the warnings use case, a new outbound settings port with a persistence adapter, new inbound use cases and a REST resource.
- **Database**: new table for rule settings plus a Liquibase changeset seeding one row per rule.
- **REST API**: new `GET /worktime/warning-rules` and `PUT /worktime/warning-rules/{rule}` in the canonical OpenAPI contract.
- **User BC and shared kernel**: `Role.ADMIN`, an admin email list in configuration, and an extended role policy. Admin assignment takes effect at the next user sync. Changing who is admin requires a config change and a redeploy.
- **Frontend**: no change required. Admins can use the endpoints directly, and a UI can follow later.
- **Behaviour**: unchanged on deploy, because the seeded values mirror today's constants.
