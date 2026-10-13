# Proposal

## Why

The ZEP REST API now accepts release-date updates: a PATCH on the employee that sends only `username` and `approval_date` is enough. MEGA still writes the release date through the SOAP `updateMitarbeiter` call, by way of the legacy `ZepService`. That keeps the hexagon user BC tied to legacy SOAP code, and it serialises what is meant to be a concurrent fan-out of ZEP updates.

## What Changes

- Write the release date to ZEP with a REST PATCH on `/employees/{username}` whose body contains only `username` and `approval_date`, instead of the SOAP `updateMitarbeiter` call.
- The hexagon ZEP employee adapter calls the ZEP REST client directly for release-date updates and no longer goes through the legacy `ZepService` for them.
- Release-date updates in the manual bulk update and in the daily auto-update become truly non-blocking. Updates for several employees are sent to ZEP concurrently instead of one after another.
- Remove the now-unused legacy release-date write: `ZepService.updateEmployeesReleaseDate` and its SOAP, REST (throwing stub) and delegating implementations, plus their tests.
- Hourly-rate updates are **not** changed. ZEP REST still cannot write internal rates, so they stay on SOAP.

No externally observable behaviour changes. The REST endpoints, scheduler timing, failure handling (a failed ZEP write leaves the local value unchanged and marks the user as failed) and the single-field write guarantee all stay the same.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. The existing `user-aggregate` requirements "Release date is written to ZEP and immediately reflected locally" and "ZEP release date writes are scoped to a single field" are transport-agnostic and remain satisfied. `release-date-auto-update`, `user-rest-api` and `zep-rest-integration` are likewise unaffected. The change sets `skip_specs: true` because it is a pure transport refactor.

## Impact

- **Code (hexagon)**: `hexagon/user/adapter/outbound/ZepEmployeeAdapter` (release-date path only).
- **Code (ZEP integration)**: `zep/rest/client/ZepEmployeeRestClient` (new PATCH method), plus a new request DTO in `zep/rest/dto`.
- **Code (legacy, removed)**: `ZepService.updateEmployeesReleaseDate`, `ZepServiceImpl`, `ZepSoapServiceImpl`, `ZepRestServiceImpl` release-date methods, and the release-date tests in `ZepServiceImplTest`.
- **External systems**: ZEP now receives REST PATCH calls instead of SOAP `updateMitarbeiter` calls for release dates. SOAP `updateMitarbeiter` remains in use for hourly rates.
- **Dependencies/config**: none new. The existing `zep` REST client configuration and auth headers are reused.
