## Why

The `uploadInternalRate` and `downloadCsvTemplate` endpoints in the legacy `EmployeeResourceImpl` have all their business logic — CSV parsing, employee existence validation, and ZEP orchestration — embedded in the REST layer. This is a missing piece of the ongoing migration into the hexagonal architecture and needs to be corrected before the legacy `EmployeeResource` can be retired.

## What Changes

- New `HourlyRate` domain value object in the `hexagon/user` BC
- New `UnknownUsersException` domain error for the user BC
- New `UpdateInternalRatesUseCase` inbound port and `UpdateInternalRatesService` application service
- New `InternalRateUpdateCommand` use case input record
- `ZepEmployeePort` extended with `updateHourlyRate(ZepUsername, HourlyRate, LocalDate)` → `Uni<Void>`
- `ZepEmployeeAdapter` implements the new outbound port method
- Two new endpoints added to `UserApi` (via OpenAPI spec) and implemented in `UserResource`:
  - `POST /users/internal-rates` — CSV upload; bulk-updates employee hourly rates in ZEP
  - `GET /users/internal-rates/csv-template` — returns a pre-populated CSV template of active employees
- Both endpoints restricted to `OFFICE_MANAGEMENT` role
- i18n delegated to the frontend; errors returned as structured codes (`errorCode`, `lines`)
- Legacy `EmployeeResourceImpl.uploadInternalRate()` and `downloadCsvTemplate()` methods removed

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `user-rest-api`: Two new endpoints added — `POST /users/internal-rates` and `GET /users/internal-rates/csv-template`.

## Impact

- **`hexagon/user`** — new domain model, use case, service, and outbound port method
- **`ZepEmployeeAdapter`** — new method wrapping legacy `ZepService.updateEmployeeHourlyRate()`
- **`UserResource` / `UserApi`** — two new endpoints; `GetActiveUsersUseCase` reused for template
- **`EmployeeResourceImpl`** — two methods removed
- **OpenAPI spec** (`openapi/paths/user.yaml`, `openapi/schemas/user.yaml`) — new paths and schemas added
