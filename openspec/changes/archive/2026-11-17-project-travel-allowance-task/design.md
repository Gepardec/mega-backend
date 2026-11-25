# Design

## Context

See proposal.md (Why). Current state in the worktime BC:

- `TravelAllowanceCalculator` treats bookings with `process == "873"` as allowance bookings. It splits passive journeys into customer trips (project in `billableProjectZepIds`) and internal trips. It emits `TRAVEL_ALLOWANCE_WRONG_PROJECT` when every passive journey is a customer trip, and `TRAVEL_ALLOWANCE_EXCEEDED` otherwise.
- `ProjectBooking.process` is ZEP's `project_task_id` as a string. ZEP attendances carry only the task id, never the task name.
- Calculators are pure (`worktime-warnings`: "Warning rules run as pure functions"). Every external input reaches them through `WorkTimeEvaluation`, which `GetEmployeeWarningsService` fills.
- The worktime BC already reads ZEP itself through its own outbound ports (bookings, absences, meal entries) on every warnings request.
- ZEP REST documents `GET /api/v1/projects/{id}/tasks`. It is paged, and each task has `id`, `name`, `project_id`, `start_date`, `end_date`, `created`, among other fields. Nothing in the repo reads it yet. `ZepProjectRestClient` already sends `limit=${mega.zep.page-size}`.
- This change is written against the state after `remove-billable-project-warning`. Once that change lands, `billableProjectZepIds` is loaded for `INTERNAL_NOTE` and `TRAVEL_ALLOWANCE` only.

## Goals / Non-Goals

**Goals:**
- Recognise a project's own travel-allowance task from current ZEP data on each warnings request.
- Check allowance per place ("Intern", and each project's own task), with the new `TRAVEL_ALLOWANCE_WRONG_PROJECT` meaning on single-place and mixed days.

**Non-Goals:**
- Storing tasks in the project aggregate or the project sync.
- Making the check aware of task creation dates. Retroactive hints are accepted (see the spec scenario "A day before the task was created is reported as well").
- Reporting a missing allowance, i.e. a passive journey with no allowance booked anywhere. Today's check doesn't do this either.
- Naming the affected project in the warning. The warning shape (`type`, `date`, `hours`) stays unchanged.
- Adjusting `INTERNAL_NOTE_MISSING` for non-billable bookings on the own task of a non-billable project. Such a booking still needs an "Intern - " marker like any non-billable booking on a non-billable project.

## Decisions

### D1: Request-time ZEP lookup in worktime, not the project sync

There is a new worktime outbound port, `WorkTimeProjectTaskZepPort.fetchTasks(Set<Integer> projectZepIds): List<ProjectTask>`, implemented by `WorkTimeProjectTaskZepAdapter` over the ZEP project service.

- **Why:** only worktime needs this fact. Worktime already depends on ZEP for every warnings request, and the data is always current. A request needs one call per booked project, usually 1 to 5. There's no Liquibase column, no aggregate field and no project-sync change.
- **Alternative: add the task id to the `Project` aggregate through project sync.** Rejected for now. It adds a migration, an aggregate field and a project-sync spec change, plus one ZEP call per project every 30 minutes for all projects, and the data is up to 30 minutes old. Revisit if a second BC needs the task. The port's shape then stays the same and only its adapter changes.

### D2: The name rule lives in the domain; the adapter returns raw tasks

- `ProjectTask(int projectZepId, int zepTaskId, String name)` is a worktime domain record.
- `TravelAllowanceTasks` is a domain value object. `TravelAllowanceTasks.from(List<ProjectTask>)` keeps the tasks whose `name.strip()` equals "Reisepauschale" ignoring case, and drops task id 873.
- It answers `ownTaskIds(projectZepId)` and `declaresOwnTask(projectZepId)`.
- **Why:** "named Reisepauschale" is a business rule from the wiki, not ZEP mapping. Dropping 873 encodes the spec rule that "Intern" declares no own task: Intern's own "Reisepauschale" is the fallback place, not a project-tracked one.
- **Alternative: filter by name in the adapter.** Rejected because it hides a domain rule in infrastructure and needs the same name constant in two places.

### D3: Evaluation input replaces billability for this rule

- `WorkTimeEvaluation` gains `TravelAllowanceTasks travelAllowanceTasks`, which is non-null; `TravelAllowanceTasks.none()` is used when the rule is inactive.
- `TravelAllowanceCalculator` no longer reads `billableProjectZepIds`.
- `GetEmployeeWarningsService` loads the tasks only when `TRAVEL_ALLOWANCE` is active and there are bookings with a project reference. It asks for the distinct `projectZepId`s of all bookings, both project bookings and journeys. Project bookings are included so that allowance booked on a project's own task is recognised even when that project has no journey that day.
- `billableProjectZepIds` is then loaded only for `INTERNAL_NOTE`.

### D4: Calculator algorithm per day

```
place(journey)  = declaresOwnTask(journey.project) ? Project(p) : INTERN
place(booking)  = process == "873"                          ? INTERN
                : process in ownTaskIds(booking.project)     ? Project(p)
                : none (not an allowance booking)

booked[place]   = sum of durations of allowance bookings at place
allowed[place]  = sum of TravelAllowancePolicy.allowanceFor(passive journeys at place)

internOver   = booked[INTERN] > allowed[INTERN]
wrongProject = internOver && any passive journey has place Project(_)
excess       = sum over Project places of max(0, booked - allowed)
             + (wrongProject ? 0 : max(0, booked[INTERN] - allowed[INTERN]))

emit TRAVEL_ALLOWANCE_WRONG_PROJECT(date, null)  if wrongProject
emit TRAVEL_ALLOWANCE_EXCEEDED(date, hours(excess)) if excess > 0
```

- Both warnings can occur on the same day: for example, a wrong place on "Intern" and an excess on a project task.
- `process` stays a `String` on `ProjectBooking`. `TravelAllowanceTasks` compares against `String.valueOf(zepTaskId)` and keeps the existing `REISEPAUSCHALE_PROCESS` constant for 873.
- **Alternative: show a hint only when a project task is also short of its allowance** (more precise on days like "30 too much on Intern, project task fully booked"). Rejected, because the user chose the simpler rule: Intern over and some journey on an own-task project.

### D5: ZEP access

- Add `ZepProjectTask` to `zep/rest/dto`, following the existing builder/`@JsonCreator` pattern.
  - It is a record with `Integer id`, `String name` and `Integer projectId` (`@JsonProperty("project_id")`).
  - `status` is deliberately not mapped, so a closed "Reisepauschale" task still counts as the project's own task (decided 2026-11-17).
  - All other fields of the response are ignored, including `description`, `billing_type`, dates, `plan_*`, `activities` and `dynamicAttributes`.
  - Observed shape (test instance, Intern = project 3): `{"id": 873, "name": "Reisepauschale", "status": "in Arbeit", "project_id": 3, "billing_type": {"id": 0, "name": "Like project"}, ...}`.
- Add `getProjectTasks(@PathParam("id") int projectId, @QueryParam("search") String search, @QueryParam("page") int page)` to `ZepProjectRestClient` under `{id}/tasks`. The class-level `limit` already applies.
- Add `ProjectService.getProjectTasks(int projectId, String search)`, which pages like `getProjectEmployeesForIdAsync`. The adapter passes `search=reisepauschale`, so ZEP returns only candidate tasks instead of a project's full task list. This matters most for "Intern", which has many tasks.
- The search only narrows the candidates. The exact rule (D2: `strip()` plus case-insensitive equality) is still applied in the domain, so a broader search result such as "Reisepauschale Kunde" is filtered out there. This relies on the search being case-insensitive and matching substrings.
- **Verified on the ZEP test instance (2026-11-17):** `GET /api/v1/projects/{Intern}/tasks?search=reisepauschale` returns the task "Reisepauschale" with `id` 873.
  - This confirms that the search ignores letter case.
  - It also confirms that a task's `id` equals the `project_task_id` on attendances: 873 is the process id the current check already reads from attendances.
  - Not verified: whether the search also finds names with surrounding whitespace, such as " Reisepauschale ". This is low risk; if it turns out to be a problem, drop the `search` parameter.
- Map a ZEP 404 to an empty list, which satisfies the scenario "A project ZEP does not know declares no own task". Other errors surface as ZEP errors and fail the request like the other worktime ZEP reads (`zep-rest-integration`).
- The adapter calls the service once per project id, one after another. A request has few projects, so making the calls in parallel isn't worth the complexity for now.
- Logging: debug-level when fetching tasks for a project (`Log.debugf`). Task names are not personal data.

### D6: `TRAVEL_ALLOWANCE_WRONG_PROJECT` keeps its identifier

The identifier stays the same and its meaning changes, so the warning-type list and OpenAPI need no change. Warnings are recalculated on every request, so no stored data carries the old meaning.

## Risks / Trade-offs

- [ZEP's `search` might not match a task name with surrounding whitespace] → Low risk because PLs normally type the name cleanly. If it happens, drop the `search` parameter; the domain rule stays the same.
- [Extra ZEP latency on the warnings request, one call per booked project] → There are usually only a few projects, and the lookup is skipped when the rule is inactive. If latency shows up, add a short-lived cache in the adapter; the port stays the same.
- [Retroactive hints for days before the task existed] → Accepted. The employee re-books, which completes the project's travel time.
- [The frontend still shows the old hint text for `TRAVEL_ALLOWANCE_WRONG_PROJECT`] → The frontend changes the text with or before this release (proposal Impact).
- [Merge order with `remove-billable-project-warning`] → Archive that change first. This change's `worktime-warnings` delta replaces the requirement version that change writes.

## Migration Plan

- No data migration and no persisted state.
- Deploy after `remove-billable-project-warning`. Coordinate the frontend hint text.
- Rollback: revert the backend release. The previous behavior returns immediately because warnings are recalculated on every request.
