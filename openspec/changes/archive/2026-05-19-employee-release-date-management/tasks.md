## 1. User Aggregate — Release Date Field

- [x] 1.1 Add nullable `releaseDate: LocalDate` to the `User` record; update all `User` factory methods and `with*` methods to carry the new field
- [x] 1.2 Add `release_date DATE NULL` column to `UserEntity`
- [x] 1.3 Add Liquibase changelog entry: `release_date DATE NULL` on `hexagon_users` (no backfill)
- [x] 1.4 Add nullable `releaseDate: LocalDate` to `ZepEmployeeSyncData`; update `ZepEmployeeMapper` to map it from `ZepEmployee.releaseDate()`
- [x] 1.5 Update `UserMapper` (MapStruct) to map `releaseDate` between `User` and `UserEntity`

## 2. ZEP REST Client — Write Support

- [x] 2.1 Add `ZepEmployeeUpdateRequest` DTO with `@JsonProperty("release_date") LocalDate releaseDate`
- [x] 2.2 Add `PUT /{username}` method to `ZepEmployeeRestClient` returning `Uni<Void>`, accepting `ZepEmployeeUpdateRequest` body

## 3. ZepEmployeePort — Release Date Write

- [x] 3.1 Add `Uni<Void> updateReleaseDate(ZepUsername username, LocalDate releaseDate)` to `ZepEmployeePort`
- [x] 3.2 Implement `updateReleaseDate` in `ZepEmployeeAdapter` — delegate to `ZepEmployeeRestClient.updateEmployee`

## 4. PayrollMonthCompletionPort

- [x] 4.1 Create `PayrollMonthCompletionPort` interface in `user/domain/port/outbound` with `Set<UserId> findUsersWithAllTasksCompleted(YearMonth month)`
- [x] 4.2 Create `PayrollMonthCompletionAdapter` in `monthend/adapter/outbound` implementing the port — delegate to `MonthEndTaskRepository.findByMonth`, group by `subjectEmployeeId`, filter where all are `DONE`

## 5. OpenAPI Spec — User Endpoints

- [x] 5.1 Create `src/main/resources/openapi/paths/user.yaml` with `GET /users/active` and `PUT /users/release-dates` path definitions
- [x] 5.2 Create `src/main/resources/openapi/schemas/user.yaml` with `ActiveUserDto` (including nullable `releaseDate`), `UpdateReleaseDateEntryDto`, `UpdateReleaseDatesRequestDto`, `UpdateReleaseDatesResponseDto`
- [x] 5.3 Register both files in `openapi.yaml` (paths + components)
- [x] 5.4 Verify code generation produces `UserApi` interface and all DTOs

## 6. Use Cases — Active Users

- [x] 6.1 Create `GetActiveUsersUseCase` inbound port in `user/application/port/inbound`
- [x] 6.2 Implement `GetActiveUsersService` — load all users from `UserRepository`, filter by `user.isActiveIn(previousPayrollMonth)`

## 7. Use Cases — Manual Release Date Update

- [x] 7.1 Create `UpdateReleaseDatesUseCase` inbound port with `UpdateReleaseDatesResult update(List<UpdateReleaseDateCommand> commands)` (command = `UserId` + `LocalDate`)
- [x] 7.2 Implement `UpdateReleaseDatesService` — look up each user's `ZepUsername` from `UserRepository`, fan out concurrently with `Multi.createFrom().iterable(...).onItem().transformToUniAndMerge(...)`, collect failed user IDs, `.await().indefinitely()`; on ZEP success, update `user.releaseDate` in `UserRepository` before collecting result

## 8. Use Case — Auto Release Date Update

- [x] 8.1 Create `AutoUpdateReleaseDatesUseCase` inbound port
- [x] 8.2 Implement `AutoUpdateReleaseDatesService` — compute payroll month as `YearMonth.now().minusMonths(1)`, call `PayrollMonthCompletionPort.findUsersWithAllTasksCompleted(payrollMonth)`, resolve each user's `ZepUsername`, set release date to `payrollMonth.atEndOfMonth()`, fan out via `Multi` with error handling per entry; on ZEP success, persist `releaseDate` locally

## 9. REST Resource — UserResource

- [x] 9.1 Create `UserResource` in `user/adapter/inbound/rest` implementing generated `UserApi`
- [x] 9.2 Implement `GET /users/active` — call `GetActiveUsersUseCase`, annotate with `@MegaRolesAllowed(Role.OFFICE_MANAGEMENT)`
- [x] 9.3 Implement `PUT /users/release-dates` — map request DTO to commands, call `UpdateReleaseDatesUseCase`, return `200` with `UpdateReleaseDatesResponseDto { failedUserIds }`; annotate with `@MegaRolesAllowed(Role.OFFICE_MANAGEMENT)`
- [x] 9.4 Add MapStruct mapper `UserRestMapper` in `user/adapter/inbound/rest` for domain ↔ DTO conversions

## 10. Scheduler

- [x] 10.1 Create `ReleaseDateAutoUpdateScheduler` in `user/adapter/inbound` with `@Scheduled(cron = "0 0 6 15-31 * ?")`, delegating to `AutoUpdateReleaseDatesUseCase`
- [x] 10.2 Add INFO logging for scheduler start/finish and each employee updated; ERROR logging for individual ZEP or local-DB failures

## 11. Tests

- [x] 11.1 Unit test `UpdateReleaseDatesService` — verify concurrent fan-out, failed ID collection, unknown user handling, local DB write after ZEP success
- [x] 11.2 Unit test `AutoUpdateReleaseDatesService` — verify correct payroll month computation, per-employee independence, ZEP failure isolation, local persistence on success
- [x] 11.3 Unit test `PayrollMonthCompletionAdapter` — verify filtering logic (all DONE → included; any OPEN → excluded)
- [x] 11.4 Integration test `UserResource` (`@QuarkusTest`) — `GET /users/active` returns `403` for non-OFFICE_MANAGEMENT and includes `releaseDate` in payload; `PUT /users/release-dates` returns `200` with expected shape
