# Proposal

## Why

Since the latest wiki update, project leads can track a project's travel time by creating a task named "Reisepauschale" on the project. Employees then book their travel allowance there instead of on "Intern". ZEP does not enforce this. An employee who books on "Intern" out of habit leaves the project's travel time incomplete, and nobody notices. The `TRAVEL_ALLOWANCE` rule only knows the "Intern" process and still follows the old convention that customer trips get their allowance on the customer project.

## What Changes

- A project declares its own travel-allowance task when it has a ZEP task named "Reisepauschale", ignoring letter case and surrounding whitespace. Tasks are read from ZEP when the warnings are requested, so a newly created task takes effect immediately.
- Each passive journey's allowance belongs on the own task of the journey's project, if the project declares one, and on "Intern"/"Reisepauschale" otherwise, whatever the project's billability. **BREAKING** (behavior): a journey on a billable project without its own task is no longer a customer trip. Its allowance on "Intern" is correct and no longer reported.
- Travel allowance booked on a project's own task counts as a travel-allowance booking. It is checked against the journeys on that project, and an excess is reported as `TRAVEL_ALLOWANCE_EXCEEDED`, like an excess on "Intern".
- `TRAVEL_ALLOWANCE_WRONG_PROJECT` keeps its identifier but gets a new meaning: a day's allowance on "Intern" exceeds what that day's journeys allow there, and at least one of the day's passive journeys is on a project with its own task. On such a day it replaces the excess on "Intern". Its old meaning ("Intern" allowance for a customer trip) is dropped. **BREAKING** for the frontend hint text, which must change to "This project tracks travel time. Please book the allowance on its 'Reisepauschale' task."
- Hints for days before the task was created are accepted: the check uses the project's current tasks for every day in the month.
- A daily cap of 2 × 30 minutes is not introduced. The per-journey allowance already limits a round trip to 60 minutes.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `worktime-warning-travel-allowance`: allowance bookings also on a project's own "Reisepauschale" task; each journey's allowance belongs on its project's own task or on "Intern"; customer trips are removed; the excess check works per place; `TRAVEL_ALLOWANCE_WRONG_PROJECT` is redefined, including on days with mixed journeys.
- `worktime-warnings`: the warnings use case also considers the travel-allowance tasks the booked projects declare in ZEP. The billability example scenario no longer uses `TRAVEL_ALLOWANCE_WRONG_PROJECT`.

Reviewed and unaffected:
- `worktime-bookings`: project bookings already carry their process reference and project.
- `worktime-warning-rule-settings`: the `TRAVEL_ALLOWANCE` rule still covers both types.
- `worktime-warning-internal-note`: uses billability independently.
- `worktime-rest-api`: the response shape is unchanged.
- `zep-rest-integration`: the new ZEP read follows the existing page-size and unknown-field rules.

## Impact

- **Depends on `remove-billable-project-warning`**: both changes modify the same `worktime-warnings` requirement. This change's delta is written against that change's version, so that change must be archived first.
- **Backend (worktime BC)**: the travel-allowance calculator, the evaluation input, the warnings service, and a new outbound port plus ZEP adapter for project tasks. Billability is no longer an input to `TRAVEL_ALLOWANCE`.
- **ZEP integration**: new read `GET /api/v1/projects/{id}/tasks` for each project the employee booked that month, on each warnings request while `TRAVEL_ALLOWANCE` is active.
- **REST API**: no structural change. `TRAVEL_ALLOWANCE_WRONG_PROJECT` keeps its identifier with a new meaning.
- **Frontend**: change the hint text for `TRAVEL_ALLOWANCE_WRONG_PROJECT`, released together with or before this backend change.
- **Project BC / project sync**: unchanged.
