## 1. Domain Model

- [x] 1.1 Create `HourlyRate` value object in `hexagon/user/domain/model/` (positive double, validates > 0)
- [x] 1.2 Create `UnknownUsersException` in `hexagon/user/domain/error/` (carries `Set<ZepUsername>`)

## 2. Application Layer

- [x] 2.1 Create `InternalRateUpdateCommand` record in `hexagon/user/application/port/inbound/` (ZepUsername, HourlyRate, LocalDate effectiveFrom)
- [x] 2.2 Create `UpdateInternalRatesUseCase` interface in `hexagon/user/application/port/inbound/`
- [x] 2.3 Create `UpdateInternalRatesService` in `hexagon/user/application/` implementing the use case: look up users via `UserRepository.findByZepUsernames()`, throw `UnknownUsersException` for any unknowns, fire ZEP updates concurrently via Mutiny

## 3. Outbound Port and Adapter

- [x] 3.1 Add `updateHourlyRate(ZepUsername, HourlyRate, LocalDate) → Uni<Void>` to `ZepEmployeePort`
- [x] 3.2 Implement the new method in `ZepEmployeeAdapter` (delegate to legacy `ZepService.updateEmployeeHourlyRate()`)

## 4. OpenAPI Spec and Code Generation

- [x] 4.1 Add `POST /users/internal-rates` path to `openapi/paths/user.yaml` (multipart/form-data request, 200/400/403/500 responses; 400 body: `errorCode` string + `lines` integer array)
- [x] 4.2 Add `GET /users/internal-rates/csv-template` path to `openapi/paths/user.yaml` (text/csv response, 200/403/500)
- [x] 4.3 Add new schemas to `openapi/schemas/user.yaml` (`InternalRateUploadError`)
- [x] 4.4 Register new paths in `openapi/openapi.yaml`
- [x] 4.5 Regenerate `UserApi` from the OpenAPI spec

## 5. REST Adapter

- [x] 5.1 Implement `POST /users/internal-rates` in `UserResource`: parse CSV bytes → `List<CsvLine(lineNumber, content)>`, validate format (column count, parseable double, parseable date), map to `List<InternalRateUpdateCommand>`, call use case, catch `UnknownUsersException` and correlate ZEP usernames back to line numbers for the error response
- [x] 5.2 Implement `GET /users/internal-rates/csv-template` in `UserResource`: call `GetActiveUsersUseCase`, format CSV string (comment header + one row per user sorted by ZEP username), return with `Content-Disposition` header

## 6. Tests

- [x] 6.1 Unit-test `HourlyRate` value object (valid construction, rejection of non-positive values)
- [x] 6.2 Unit-test `UpdateInternalRatesService` (all users known → ZEP called; unknown users → exception thrown with correct usernames)
- [x] 6.3 Integration-test `POST /users/internal-rates` via REST-Assured (valid CSV → 200; empty file → 400 EMPTY_FILE; bad format → 400 BAD_FORMAT with correct lines; unknown user → 400 UNKNOWN_USERS with correct lines; wrong role → 403)
- [x] 6.4 Integration-test `GET /users/internal-rates/csv-template` via REST-Assured (correct header, correct rows, correct Content-Disposition; wrong role → 403)
