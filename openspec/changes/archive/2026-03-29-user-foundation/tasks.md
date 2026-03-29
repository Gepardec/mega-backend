## 1. Domain Model

- [x] 1.1 Create `UserId` value object (wraps UUID)
- [x] 1.2 Create `Email` value object (wraps String)
- [x] 1.3 Create `FullName` value object (firstname, lastname)
- [x] 1.4 Create `UserStatus` enum (ACTIVE, INACTIVE)
- [x] 1.5 Create `Role` enum (EMPLOYEE, OFFICE_MANAGEMENT, PROJECT_LEAD)
- [x] 1.6 Create `EmploymentPeriod` record (start, end LocalDate)
- [x] 1.7 Create `RegularWorkingTime` record (start LocalDate, workingHours Map<DayOfWeek, Duration>)
- [x] 1.8 Create `ZepProfile` value object (username, firstname, lastname, title, salutation, workDescription, language, releaseDate, employmentPeriods, regularWorkingTimes)
- [x] 1.9 Create `PersonioProfile` value object (personioId, vacationDayBalance, guildLead, internalProjectLead, hasCreditCard)
- [x] 1.10 Create `User` aggregate root with fields: id, email, name, status, roles, zepProfile, personioProfile
- [x] 1.11 Add `User.create(UserId, ZepProfile, Set<Role>)` factory method
- [x] 1.12 Add `User.syncFromZep(ZepProfile)` method
- [x] 1.13 Add `User.syncFromPersonio(PersonioProfile)` method — must NOT clear existing profile if arg is null

## 2. Outbound Ports

- [x] 2.1 Create `ZepEmployeePort` interface with `List<ZepProfile> fetchAll()`
- [x] 2.2 Create `PersonioEmployeePort` interface with `Optional<PersonioProfile> findByEmail(Email)`
- [x] 2.3 Create `UserRepository` interface with `Optional<User> findByZepUsername(String)`, `List<User> findAll()`, `void saveAll(List<User>)`

## 3. Inbound Port

- [x] 3.1 Create `SyncUsersUseCase` interface with `void sync()`

## 4. Application Layer

- [x] 4.1 Create `UserSyncConfig` record with `List<String> officeManagementUsernames()`
- [x] 4.2 Create `SyncUsersService` implementing `SyncUsersUseCase` — inject `ZepEmployeePort`, `PersonioEmployeePort`, `UserRepository`, `UserSyncConfig`
- [x] 4.3 Implement sync logic: fetch ZEP employees, find-or-create Users by ZEP username, assign roles, best-effort Personio enrichment, deactivate absent users, saveAll

## 5. Outbound Adapters

- [x] 5.1 Create `ZepEmployeeAdapter` implementing `ZepEmployeePort` — delegate to existing `ZepEmployeeRestClient`, map `ZepEmployee` → `ZepProfile`
- [x] 5.2 Create `PersonioEmployeeAdapter` implementing `PersonioEmployeePort` — delegate to existing `PersonioEmployeesClient`, map `PersonioEmployeeDto` → `PersonioProfile`
- [x] 5.3 Create `UserEntity` JPA entity for `hexagon_users` table (columns for all User fields including JSON or relational storage for ZepProfile/PersonioProfile)
- [x] 5.4 Create `UserRepositoryAdapter` implementing `UserRepository` using Panache — implement findByZepUsername, findAll, saveAll with UserEntity ↔ User mapping

## 6. Persistence

- [x] 6.1 Add Liquibase changelog `src/main/resources/db/changelog/hexagon/001-user-foundation.yaml` creating the `hexagon_users` table

## 7. Inbound Adapter

- [x] 7.1 Create `UserSyncScheduler` with `@Scheduled(every = "PT30M")` calling `SyncUsersUseCase.sync()`
- [x] 7.2 Wire `UserSyncConfig` from Quarkus `@ConfigProperty` in the scheduler or a CDI producer (e.g. `mega.hexagon.user.office-management-usernames`)
- [x] 7.3 Add config property `mega.hexagon.user.office-management-usernames` to `application.yaml` with an empty default list

## 8. Tests

- [x] 8.1 Unit test `User` aggregate: create, syncFromZep, syncFromPersonio (including null-preservation)
- [x] 8.2 Unit test `SyncUsersService`: new user creation, update, deactivation, role assignment, best-effort Personio enrichment
- [x] 8.3 Integration test `UserRepositoryAdapter`: findByZepUsername, saveAll using H2 test profile
- [x] 8.4 Verify `SyncUsersService` has no Quarkus/JPA imports
