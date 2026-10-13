# Design

## Context

See proposal.md for the motivation.

Current release-date write path:

```
UpdateReleaseDatesService / AutoUpdateReleaseDatesService
  |  Multi.transformToUniAndMerge(updateInZep)  -> collect -> await
  v
ZepEmployeePort.updateReleaseDate(ZepUsername, LocalDate) : Uni<Void>
  v
ZepEmployeeAdapter  -- Uni.createFrom().item(() -> blocking call)
  v
legacy ZepService (ZepServiceImpl -> @Soap delegate)
  v
ZepSoapServiceImpl.updateEmployeesReleaseDate  -- SOAP updateMitarbeiter { userId, freigabedatum }
```

Constraints:
- The port is already reactive (`Uni<Void>`), and both use cases already turn a failed `Uni` into a per-user failure. They resolve users on the calling thread before the fan-out and persist only after `await()`. That shape was introduced to avoid the JTA context propagation race fixed in #830.
- `ZepEmployeeRestClient` is a Quarkus reactive REST client (`configKey = "zep"`, `AuthHeaders`, class-level `@ClientQueryParam(limit = ${mega.zep.page-size})`).
- ZEP's REST API accepts `PATCH /employees/{username}` with a body of only `{"username": "...", "approval_date": "YYYY-MM-DD"}` and updates the release date. This was verified manually against ZEP. Internal (hourly) rates still cannot be written over REST.
- Nothing outside `zep/impl` calls `ZepService.updateEmployeesReleaseDate` or the cached SOAP `getEmployees()`.

## Goals / Non-Goals

**Goals:**
- Release-date writes go from the hexagon adapter straight to the ZEP REST client, with no legacy `ZepService` hop.
- The adapter returns the REST client's `Uni` as is, so the existing fan-out sends updates concurrently.
- The PATCH body is guaranteed to contain only `username` and `approval_date`.

**Non-Goals:**
- Changing `ZepEmployeePort`, the use cases, or their failure/retry semantics.
- Moving hourly-rate updates off SOAP, or otherwise touching `updateMitarbeiter` for internal rates.
- Retrying or handling ZEP 429 specially. Per `zep-rest-integration`, ZEP errors surface as-is.

## Decisions

### D1: Dedicated request DTO with `username` and `approval_date`
Add a small record `ZepEmployeeReleaseDateUpdate(String username, @JsonProperty("approval_date") LocalDate releaseDate)` in `zep/rest/dto`, and send it as the PATCH body. Its component and JSON names match `ZepEmployee` (`username` → `username`, `releaseDate` → `approval_date`), so the read and write DTOs use the same names.

- The body repeats `username` even though it is also in the URL. This mirrors the request shape that was verified against ZEP, which identifies the employee in the body as well.

- *Alternative: reuse `ZepEmployee`.* Rejected. Serialising the full record would either send nulls for every other field (risking overwrites in ZEP) or depend on a global `NON_NULL` setting. Either way, the single-field guarantee (`user-aggregate`: "ZEP release date writes are scoped to a single field") would rest on configuration instead of on the type.
- *Alternative: `Map<String, Object>` body.* Rejected. It is untyped and hides the field name in a string literal.
- The `LocalDate` serialises as ISO `YYYY-MM-DD` through the existing Jackson setup, the same format `ZepEmployee` reads `approval_date` with.

### D2: PATCH method on the existing `ZepEmployeeRestClient`
Add `@PATCH @Path("/{username}") Uni<Void> updateReleaseDate(@PathParam("username") String username, ZepEmployeeReleaseDateUpdate body)` (a JSON content type is assumed or declared explicitly).

- The method is named after what its body can change, not after the HTTP resource. It matches `ZepEmployeePort.updateReleaseDate`. A future write such as internal rates would get its own method and body against the same endpoint.

- `Uni<Void>` ignores the response body, so it works the same whether ZEP answers `200` with the employee or `204`. MEGA does not need the response, because the local value comes from the command.
- HTTP status >= 400 makes the `Uni` fail (default reactive REST client behaviour). The use cases already recover from that per user.
- The class-level `limit` query parameter is also sent on the PATCH. ZEP ignores it for a single-resource PATCH, and it matches the `zep-rest-integration` requirement that every request carries the page size. No special handling is needed.
- *Alternative: a separate client interface just for writes.* Rejected because it would only duplicate the config key and auth headers.

### D3: Adapter calls the REST client directly; no service in between
`ZepEmployeeAdapter.updateReleaseDate` injects `@RestClient ZepEmployeeRestClient` and returns `client.updateReleaseDate(username.value(), new ZepEmployeeReleaseDateUpdate(username.value(), releaseDate))`. It logs the outgoing call at INFO and does not log personal data beyond the ZEP username.

- *Alternative: add a method to the legacy `zep/rest/service/EmployeeService`.* Rejected. That service is a blocking, legacy-facing read helper, and adding writes there would keep the hexagon coupled to legacy code for no gain. The adapter already calls REST services directly for reads.
- The adapter keeps its `ZepService` injection only for `updateHourlyRate`.

### D4: Delete the legacy release-date write entirely
Remove `updateEmployeesReleaseDate` from `ZepService`, `ZepServiceImpl`, `ZepSoapServiceImpl` (including its `@CacheInvalidate("employee")`) and `ZepRestServiceImpl`, together with the four release-date tests in `ZepServiceImplTest`.

- The cache invalidation is not needed: the only reader of the `employee` cache is SOAP `getEmployees()`, and nothing outside `zep/impl` calls it.
- *Alternative: keep the SOAP path as a fallback.* Rejected. Nothing would call it, and it would rot.

## Risks / Trade-offs

- [The fan-out now really runs concurrently, so a month-end run sends many PATCHes at once] → The earlier rate-limit probe showed no ZEP throttling on bursts, and per-user failures are already retried by the next daily run. Watch the first production month-end run.
- [The REST behaviour was verified manually, not documented by ZEP] → The adapter test pins the request shape. If ZEP changes the endpoint, failures surface as per-user errors with logged ZEP status codes, and the local release date stays unchanged.
- [Reactive client callbacks run on I/O threads] → No change needed. The use cases already avoid touching CDI/JTA state inside the fan-out (#830 shape), and the adapter does no persistence.

## Migration Plan

Deploy as a normal release. No data or config migration is needed. Rollback means reverting the commit, which restores the SOAP path.
