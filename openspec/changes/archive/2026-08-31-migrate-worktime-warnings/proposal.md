## Why

The employee time-booking warning feature — the twelve calculators centred in `com.gepardec.mega.domain.calculation` — is the last major capability still living in the legacy backend. The calculation logic itself is sound and well-tested, but it is wrapped in accidental complexity: an orchestration manager that computes localized message strings the frontend never consumes, mutable warning models with merge-by-date behaviour, a wall-clock (`now()`) dependency inside the domain, and an input model built on getter/builder classes. Moving the feature into the `worktime` bounded context retires the legacy path, preserves the proven calculation logic (and its git history), and sheds the tech debt around it.

## What Changes

- Introduce a **warning calculation capability** in the `worktime` BC: for an authenticated employee and payroll month, fetch that employee's detailed time bookings and absences, run the warning calculators, and return the resulting warnings.
- **Move** the twelve warning calculators into the `worktime` domain, preserving their decision logic. Their inputs, outputs, and warning-construction code are adapted to the new models; the algorithms are unchanged.
- Introduce a **detailed time-booking model**: a per-slot booking, distinguished as a project booking or a journey booking, carrying start/end time, activity, working location, project-relevance, journey direction, and vehicle. This is distinct from the existing aggregate work-time attendance (which only carries billable/non-billable hours).
- Introduce a **warning model** in which a warning is a domain fact of `type`, `date`, and an optional `hours` quantity. **BREAKING (internal):** the backend no longer computes localized warning text — presentation/localization becomes a frontend (transloco) concern. Warning **type identifiers are preserved** so existing frontend translation keys keep resolving.
- Reframe the **"no time entry" warning** as a set difference over an employee's *expected working days* — office-calendar working days intersected with the active employment period and the employee's non-zero-hour weekdays — resolved through an outbound port. "Today" is taken from an injected clock instead of a direct wall-clock call.
- Add a new REST endpoint **`GET /worktime/warnings/{payrollMonth}`**, secured to the EMPLOYEE role and scoped to the authenticated employee, returning a flat list of warnings.
- **Remove** the legacy warnings path once the calculators are moved: the warnings orchestration manager, the legacy time-warning service, the legacy warning models and their REST DTO/mapper, and the legacy warnings endpoint. **BREAKING (API):** the legacy warnings endpoint is retired; the frontend must repoint to the new endpoint as part of this change.

## Capabilities

### New Capabilities
- `worktime-warnings`: The warning calculation use case, the warning domain model and warning-type vocabulary, the migrated calculators, the warning assembly domain service, and the expected-working-days resolution behind the "no time entry" rule.
- `worktime-bookings`: The detailed per-slot time-booking domain model (project vs journey) and the ZEP outbound fetch and mapping that produces it for an employee and payroll month.

### Modified Capabilities
- `worktime-rest-api`: Add the authenticated-employee warnings endpoint (self-scoped, EMPLOYEE role) to the canonical work time REST contract.

## Impact

- **New (hexagon `worktime` BC):** warnings use case + inbound REST adapter, warning domain model + warning-type vocabulary, detailed-booking domain model, detailed-booking ZEP outbound port + adapter, expected-working-days outbound port + adapter, warning assembly domain service; the twelve calculators relocated into the worktime domain.
- **Reused (hexagon `worktime` BC):** authenticated-actor/self-scoping, payroll-month transport parsing, absence outbound port + `Absence`/`AbsenceType` model, user-snapshot outbound port, the shared office-calendar utility, and employment-period/regular-working-time data (via an outbound port into user master data).
- **OpenAPI contract:** new path and warning schema added under the canonical work time contract; new generated API interface + HTTP model.
- **Removed (legacy):** warnings orchestration manager, legacy time-warning service (API + impl), legacy warning models and warning-type enums, legacy warnings REST DTO + mapper, and the legacy warnings endpoint method.
- **Unchanged (legacy):** the legacy `ProjectEntry` booking family and `getProjectTimes` remain — still used by monthly report generation.
- **Frontend (out-of-repo, coordinated):** repoint the warnings request to the new endpoint; group the flat warning list by type for chip rendering; keep using the existing warning-type translation keys.
