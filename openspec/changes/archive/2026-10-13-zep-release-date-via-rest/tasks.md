# Tasks

## 1. ZEP REST client: release-date PATCH

- [x] 1.1 Add the `ZepEmployeeReleaseDateUpdate` request record to `zep/rest/dto` with the components `username` (`String`) and `releaseDate` (`LocalDate`, JSON name `approval_date`), named as in `ZepEmployee` (design D1). Add a unit test that serialises it with the application `ObjectMapper` and asserts the JSON is exactly `{"username":"007-jbond","approval_date":"2026-09-30"}`. Verify with `mvn test -Dtest=ZepEmployeeReleaseDateUpdateTest`.
- [x] 1.2 Add `@PATCH @Path("/{username}") Uni<Void> updateReleaseDate(String username, ZepEmployeeReleaseDateUpdate body)` with a JSON content type to `ZepEmployeeRestClient` (design D2). Verify the project compiles (`mvn compile`).

## 2. Hexagon adapter switches to REST

- [x] 2.1 Change `ZepEmployeeAdapter.updateReleaseDate` to call the injected `@RestClient ZepEmployeeRestClient` and return its `Uni` directly, with an INFO log of the outgoing ZEP call (design D3). Keep `ZepService` only for `updateHourlyRate`. Verify with 2.2.
- [x] 2.2 Add a unit test for `ZepEmployeeAdapter` (JUnit 5 + Mockito, Instancio where useful) covering: the REST client is called with the ZEP username as path parameter and a body carrying that same username and the release date; a failed client `Uni` (e.g. `WebApplicationException` 404) makes the returned `Uni` fail; and the legacy `ZepService.updateEmployeesReleaseDate` is no longer used. Verify with `mvn test -Dtest=ZepEmployeeAdapterTest`.
- [x] 2.3 Confirm the existing use-case tests still pass unchanged (`mvn test -Dtest='UpdateReleaseDatesServiceTest,AutoUpdateReleaseDatesServiceTest'`).

## 3. Remove the legacy SOAP release-date write

- [x] 3.1 Remove `updateEmployeesReleaseDate` from `ZepService`, `ZepServiceImpl`, `ZepSoapServiceImpl` (including its `@CacheInvalidate`) and `ZepRestServiceImpl`, plus any imports that become unused (design D4). Verify `grep -rn updateEmployeesReleaseDate src/` returns nothing.
- [x] 3.2 Delete the four release-date tests in `ZepServiceImplTest` (`updateEmployeesReleaseDate*`) and keep the hourly-rate test. Verify with `mvn test -Dtest=ZepServiceImplTest`.

## 4. Integration check

- [x] 4.1 Run the full build including `ArchitectureTest` and verify it passes (`mvn clean package`).
- [x] 4.2 Against the ZEP test instance, update one employee's release date through `PUT /users/release-dates` in dev mode and verify the new date shows up in ZEP and in the local user record.
