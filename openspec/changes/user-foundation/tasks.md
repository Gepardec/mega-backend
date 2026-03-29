## 1. Domain Model

- [ ] 1.1 Create `UserId` value object (wraps UUID)
- [ ] 1.2 Create `Email` value object (wraps String)
- [ ] 1.3 Create `FullName` value object (firstname, lastname)
- [ ] 1.4 Create `UserStatus` enum (ACTIVE, INACTIVE)
- [ ] 1.5 Create `Role` enum (EMPLOYEE, OFFICE_MANAGEMENT, PROJECT_LEAD)
- [ ] 1.6 Create `EmploymentPeriod` record (start, end LocalDate)
- [ ] 1.7 Create `RegularWorkingTime` record (start LocalDate, workingHours Map<DayOfWeek, Duration>)
- [ ] 1.8 Create `ZepProfile` value object (username, firstname, lastname, title, salutation, workDescription, language, releaseDate, employmentPeriods, regularWorkingTimes)
- [ ] 1.9 Create `PersonioProfile` value object (personioId, vacationDayBalance, guildLead, internalProjectLead, hasCreditCard)
- [ ] 1.10 Create `User` aggregate root with fields: id, email, name, status, roles, zepProfile, personioProfile
- [ ] 1.11 Add `User.create(UserId, ZepProfile, Set<Role>)` factory method
- [ ] 1.12 Add `User.syncFromZep(ZepProfile)` method
- [ ] 1.13 Add `User.syncFromPersonio(PersonioProfile)` method — must NOT clear existing profile if arg is null

## 2. Outbound Ports

- [ ] 2.1 Create `ZepEmployeePort` interface with `List<ZepProfile> fetchAll()`
- [ ] 2.2 Create `PersonioEmployeePort` interface with `Optional<PersonioProfile> findByEmail(Email)`
- [ ] 2.3 Create `UserRepository` interface with `Optional<User> findByZepUsername(String)`, `List<User> findAll()`, `void saveAll(List<User>)`

## 3. Inbound Port

- [ ] 3.1 Create `SyncUsersUseCase` interface with `void sync()`

## 4. Application Layer

- [ ] 4.1 Create `UserSyncConfig` record with `List<String> officeManagementUsernames()`
- [ ] 4.2 Create `SyncUsersService` implementing `SyncUsersUseCase` — inject `ZepEmployeePort`, `PersonioEmployeePort`, `UserRepository`, `UserSyncConfig`
- [ ] 4.3 Implement sync logic: fetch ZEP employees, find-or-create Users by ZEP username, assign roles, best-effort Personio enrichment, deactivate absent users, saveAll

## 5. Outbound Adapters

- [ ] 5.1 Create `ZepEmployeeAdapter` implementing `ZepEmployeePort` — delegate to existing `ZepEmployeeRestClient`, map `ZepEmployee` → `ZepProfile`
- [ ] 5.2 Create `PersonioEmployeeAdapter` implementing `PersonioEmployeePort` — delegate to existing `PersonioEmployeesClient`, map `PersonioEmployeeDto` → `PersonioProfile`
- [ ] 5.3 Create `UserEntity` JPA entity for `hexagon_users` table (columns for all User fields including JSON or relational storage for ZepProfile/PersonioProfile)
- [ ] 5.4 Create `UserRepositoryAdapter` implementing `UserRepository` using Panache — implement findByZepUsername, findAll, saveAll with UserEntity ↔ User mapping

## 6. Persistence

- [ ] 6.1 Add Liquibase changelog `src/main/resources/db/changelog/hexagon/001-user-foundation.yaml` creating the `hexagon_users` table

## 7. Inbound Adapter

- [ ] 7.1 Create `UserSyncScheduler` with `@Scheduled(every = "PT30M")` calling `SyncUsersUseCase.sync()`
- [ ] 7.2 Wire `UserSyncConfig` from Quarkus `@ConfigProperty` in the scheduler or a CDI producer (e.g. `mega.hexagon.user.office-management-usernames`)
- [ ] 7.3 Add config property `mega.hexagon.user.office-management-usernames` to `application.yaml` with an empty default list

## 8. Tests

- [ ] 8.1 Unit test `User` aggregate: create, syncFromZep, syncFromPersonio (including null-preservation)
- [ ] 8.2 Unit test `SyncUsersService`: new user creation, update, deactivation, role assignment, best-effort Personio enrichment
- [ ] 8.3 Integration test `UserRepositoryAdapter`: findByZepUsername, saveAll using H2 test profile
- [ ] 8.4 Verify `SyncUsersService` has no Quarkus/JPA imports
