## Why

The `updateEmployeesReleaseDate` capability exists only in the legacy `com.gepardec.mega` stack, hidden behind SOAP and a CompletableFuture-based fan-out. Migrating it to the hexagon unlocks a clean REST-first API for office management staff to update employee release dates, removes the SOAP dependency for this operation, and enables a scheduled automation that sets release dates automatically once all month-end tasks for the payroll month are complete.

## What Changes

- New REST endpoints in the hexagon `user` BC under `/users`:
  - `GET /users/active` — returns employees active during the previous payroll month (OFFICE_MANAGEMENT only)
  - `PUT /users/release-dates` — bulk-updates ZEP release dates for a list of `{userId, releaseDate}` entries (OFFICE_MANAGEMENT only), returns `200` with a list of failed user IDs
- New `PUT /{username}` method on `ZepEmployeeRestClient` with a minimal `ZepEmployeeUpdateRequest` body (`release_date`)
- New `updateReleaseDate(ZepUsername, LocalDate)` method on `ZepEmployeePort` returning `Uni<Void>`; concurrent fan-out in the service using Mutiny `Multi`; on success, the release date is also persisted locally (dual-write)
- New outbound port `PayrollMonthCompletionPort` in the `user` BC, implemented by the `monthend` BC adapter; exposes `findUsersWithAllTasksCompleted(YearMonth)` → `Set<UserId>`
- New `ReleaseDateAutoUpdateScheduler` in the `user` BC inbound adapter — daily cron `0 0 6 15-31 * ?`; per-employee independent check: as soon as all tasks for an employee are done, update their release date to the last day of the payroll month
- Old `EmployeeServiceImpl.updateEmployeesReleaseDate` and related code is **not modified**

## Capabilities

### New Capabilities

- `user-rest-api`: OpenAPI-spec-first REST endpoints for reading active employees and bulk-updating their ZEP release dates, restricted to OFFICE_MANAGEMENT; this is the hexagonal home for the full `EmployeeResource` migration — `bulkUpdate` and `csvTemplate` are future slices of the same capability
- `release-date-auto-update`: Scheduled job that checks month-end task completion per employee from the 15th of the month and auto-sets their ZEP release date to the last day of the previous (payroll) month

### Modified Capabilities

- `user-aggregate`: `ZepEmployeePort` gains a write method (`updateReleaseDate`); `release_date` is added to the `User` aggregate, `ZepEmployeeSyncData`, `UserEntity`, and a new Liquibase migration; dual-write ensures local DB stays consistent after each ZEP update
- `user-sync`: `ZepEmployeeSyncData` now carries `release_date`; the sync flow persists it on `User`; two existing requirement scenarios updated to reflect the added field

## Impact

- **REST API**: two new endpoints under `/users`, generated from OpenAPI spec (`UserApi` interface)
- **ZEP REST client**: new `PUT /{username}` method + request DTO; REST transport attempted first; SOAP remains available as fallback if the ZEP REST endpoint is not available
- **user BC**: two new use cases (`GetActiveUsersUseCase`, `UpdateReleaseDatesUseCase`), one new auto-update use case (`AutoUpdateReleaseDatesUseCase`), one new outbound port (`PayrollMonthCompletionPort`), one new scheduler
- **monthend BC**: new adapter class implementing `PayrollMonthCompletionPort` backed by `MonthEndTaskRepository`
- **No legacy code modified**
