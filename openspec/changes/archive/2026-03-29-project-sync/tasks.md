## 1. Database Migration

- [x] 1.1 Add Liquibase changeset to create `hexagon_projects` table (id UUID PK, zep_id INTEGER, name VARCHAR UNIQUE NOT NULL, start_date DATE NOT NULL, end_date DATE)
- [x] 1.2 Add Liquibase changeset to create `hexagon_project_leads` join table (project_id UUID FK → hexagon_projects.id, user_id UUID FK → hexagon_users.id, PRIMARY KEY (project_id, user_id))

## 2. Project Domain Model

- [x] 2.1 Create `ProjectId` value object (wraps UUID, static `generate()` and `of(UUID)` factory methods)
- [x] 2.2 Create `ZepProjectProfile` record (zepId, name, startDate, endDate)
- [x] 2.3 Create `Project` aggregate with `create(ProjectId, ZepProjectProfile)` and `reconstitute(...)` factory methods; leads field as `Set<UserId>`
- [x] 2.4 Add `syncFromZep(ZepProjectProfile)` method to `Project` for updating mutable fields
- [x] 2.5 Add `setLeads(Set<UserId>)` method to `Project`

## 3. Project Domain Ports

- [x] 3.1 Create `SyncProjectsUseCase` inbound port interface (`void sync()`)
- [x] 3.2 Create `ReconcileLeadsUseCase` inbound port interface (`void reconcile()`)
- [x] 3.3 Create `ZepProjectPort` outbound port interface (`List<ZepProjectProfile> fetchAll()`, `List<String> fetchLeadUsernames(int zepId)`)
- [x] 3.4 Create `ProjectRepository` outbound port interface (`Optional<Project> findByZepId(int zepId)`, `List<Project> findAll()`, `void saveAll(List<Project> projects)`)
- [x] 3.5 Create `UserLookupPort` outbound port interface in project domain (`Optional<UserId> findUserIdByZepUsername(String username)`)

## 4. SyncProjectsUseCase Implementation

- [x] 4.1 Create `SyncProjectsService` implementing `SyncProjectsUseCase`; constructor-inject `ZepProjectPort` and `ProjectRepository`
- [x] 4.2 Implement `sync()`: fetch all from ZEP, upsert by zepId (create if absent, `syncFromZep` if present), call `ProjectRepository.saveAll()`

## 5. ReconcileLeadsUseCase Implementation

- [x] 5.1 Create `ReconcileLeadsService` implementing `ReconcileLeadsUseCase`; constructor-inject `ZepProjectPort`, `ProjectRepository`, `UserLookupPort`, and user domain's `UserRepository`
- [x] 5.2 Implement `reconcile()`: for each project, fetch lead usernames from ZEP, resolve to UserIds via `UserLookupPort`, call `project.setLeads(resolvedIds)`
- [x] 5.3 Implement `PROJECT_LEAD` role assignment: collect all UserIds that are leads on any project; add `PROJECT_LEAD` to those users, remove from users no longer leading any project; call `UserRepository.saveAll()`

## 6. Outbound Adapters

- [x] 6.1 Create `ZepProjectAdapter` implementing `ZepProjectPort`; add `getAllProjects()` to `ProjectService` (omits date params — optional per ZEP API); use `ProjectService` for both `fetchAll()` and `fetchLeadUsernames()`
- [x] 6.2 Verify which `ZepProjectEmployeeType` id value(s) represent leads (check `ProjectEmployeesMapper` in legacy code); filter accordingly in `fetchLeadUsernames`
- [x] 6.3 Create `ProjectEntity` JPA entity mapping to `hexagon_projects` table
- [x] 6.4 Create `ProjectPanacheRepository` extending `PanacheRepository<ProjectEntity>`
- [x] 6.5 Create `ProjectRepositoryAdapter` implementing `ProjectRepository`; handle `Project ↔ ProjectEntity` mapping; upsert logic in `saveAll`
- [x] 6.6 Create `UserLookupAdapter` implementing `UserLookupPort`; query `hexagon_users` by `zep_username` column via a Panache query

## 7. Unified SyncScheduler

- [x] 7.1 Create `SyncScheduler` in `application/schedule/` annotated `@ApplicationScoped`; inject `SyncUsersUseCase`, `SyncProjectsUseCase`, `ReconcileLeadsUseCase`
- [x] 7.2 Implement `@Scheduled(every = "PT30M", delay = 15, delayUnit = SECONDS)` method that calls the three use cases in sequence
- [x] 7.3 Delete `hexagon/user/adapter/inbound/UserSyncScheduler.java`

## 8. Tests

- [x] 8.1 Unit test `SyncProjectsService`: verify upsert-by-zepId logic, new project creation, existing project update, no lead mutation
- [x] 8.2 Unit test `ReconcileLeadsService`: verify lead resolution, unknown username skipping, full leads-set replacement, `PROJECT_LEAD` role gain and revocation
- [x] 8.3 Unit test `Project` aggregate: create, reconstitute, syncFromZep, setLeads
- [x] 8.4 Unit test `ZepProjectAdapter`: verify pagination handling and lead type filtering
