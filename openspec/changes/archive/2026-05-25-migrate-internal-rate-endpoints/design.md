## Context

The `uploadInternalRate` and `downloadCsvTemplate` operations currently live in `EmployeeResourceImpl` in the legacy REST layer. All logic — CSV stream parsing, column format validation, employee existence checking, and ZEP orchestration — is embedded directly in the resource class. This violates the hexagonal architecture's responsibility boundaries and blocks the eventual retirement of the legacy `EmployeeResource`.

The `hexagon/user` BC already contains `UserRepository` (with `findByZepUsernames`), `ZepEmployeePort`, `GetActiveUsersUseCase`, and `UserResource`/`UserApi`. All the infrastructure needed for this migration exists; it only needs to be wired together correctly.

## Goals / Non-Goals

**Goals:**
- Move the two endpoints into the hexagonal `user` BC with correct layer responsibilities
- Introduce `HourlyRate` as a typed domain value object
- Parallel ZEP update calls for throughput, consistent with the existing release-date update pattern
- Structured error codes for unknown-user failures, delegating i18n to the frontend

**Non-Goals:**
- Changing the observable API contract (request/response shapes stay equivalent)
- Adding retry logic for ZEP failures
- Migrating any other `EmployeeResource` endpoints in this change

## Decisions

### CSV parsing and format validation belong in the REST adapter

CSV is a transport format. Parsing the byte stream into structured data and validating column structure (count, parseable number, parseable date) is input validation at the system boundary — the same category as JSON schema validation. The use case receives a clean list of `InternalRateUpdateCommand` objects; it never sees raw CSV bytes.

_Alternative considered_: A dedicated CSV parsing service in the application layer. Rejected — it would make the application layer aware of a transport format with no benefit.

### Employee existence check belongs in the application service

Validating that every ZEP username in the request maps to a known user is a business rule, not an input format check. The application service performs this check via `UserRepository.findByZepUsernames()` before issuing any ZEP calls. If any usernames are unknown, it throws `UnknownUsersException(Set<ZepUsername>)` and no ZEP updates are issued.

_Alternative considered_: Validate existence in the REST adapter. Rejected — business rules belong in the application layer.

### Error line numbers are correlated in the REST adapter

The use case reports *which ZEP usernames* are unknown, not which CSV line numbers. The REST adapter, which already holds the parsed `CsvLine(lineNumber, content)` list, correlates the returned unknown usernames back to their original line numbers before building the HTTP error response. This keeps CSV line numbers out of the domain model entirely.

### No new use case for the CSV template

The template endpoint needs only the list of active users and the current date. `GetActiveUsersUseCase` already provides the former, and the current date is a trivial local lookup. The REST adapter formats the CSV string directly. Creating a dedicated use case would be a trivially thin wrapper over an already-existing one.

### Endpoints added to the existing `UserApi` and `UserResource`

Both new endpoints belong to the same user BC and the same role (`OFFICE_MANAGEMENT`). Adding them to the existing `UserResource` keeps the API cohesive and avoids a new resource class for two closely related operations.

### ZEP updates are parallelised

Once all validations pass, ZEP hourly-rate updates are fired concurrently — consistent with the release-date update pattern in the same BC. A failure in one ZEP call surfaces as a 500 at the request level; partial-success reporting is out of scope for this change.

### `HourlyRate` as a domain value object

Typed value objects are the established convention throughout the `hexagon/user` domain model (`ZepUsername`, `UserId`, `Email`, etc.). `HourlyRate` encapsulates the constraint that a rate must be positive.

## Risks / Trade-offs

- **Concurrent ZEP calls under load** — parallelising all updates in a single request could spike ZEP SOAP connections for large CSV uploads. Mitigation: ZEP's SOAP connection pool is shared across the application; the risk is low for the expected batch sizes (tens of employees).
- **Partial ZEP failure** — if some ZEP calls succeed and some fail after validation passes, the caller receives a 500 with no indication of which updates completed. Mitigation: acceptable for now; the use case can be extended to return a partial-failure result if needed.
- **Parallel legacy endpoints** — both the legacy and hexagon endpoints will be live simultaneously until the legacy ones are removed. Mitigation: removal of the legacy methods is a task in this change.

