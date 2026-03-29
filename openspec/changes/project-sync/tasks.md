## 1. Database Migration

- [ ] 1.1 Add Liquibase changeset to create `hexagon_projects` table (id UUID PK, zep_id INTEGER, name VARCHAR UNIQUE NOT NULL, start_date DATE NOT NULL, end_date DATE)
- [ ] 1.2 Add Liquibase changeset to create `hexagon_project_leads` join table (project_id UUID FK → hexagon_projects.id, user_id UUID FK → hexagon_users.id, PRIMARY KEY (project_id, user_id))

## 2. Project Domain Model

- [ ] 2.1 Create `ProjectId` value object (wraps UUID, static `generate()` and `of(UUID)` factory methods)
- [ ] 2.2 Create `ZepProjectProfile` record (zepId, name, startDate, endDate)
- [ ] 2.3 Create `Project` aggregate with `create(ProjectId, ZepProjectProfile)` and `reconstitute(...)` factory methods; leads field as `Set<UserId>`
- [ ] 2.4 Add `syncFromZep(ZepProjectProfile)` method to `Project` for updating mutable fields
- [ ] 2.5 Add `setLeads(Set<UserId>)` method to `Project`

## 3. Project Domain Ports

- [ ] 3.1 Create `SyncProjectsUseCase` inbound port interface (`void sync()`)
- [ ] 3.2 Create `ReconcileLeadsUseCase` inbound port interface (`void reconcile()`)
- [ ] 3.3 Create `ZepProjectPort` outbound port interface (`List<ZepProjectProfile> fetchAll()`, `List<String> fetchLeadUsernames(int zepId)`)
- [ ] 3.4 Create `ProjectRepository` outbound port interface (`Optional<Project> findByZepId(int zepId)`, `List<Project> findAll()`, `void saveAll(List<Project> projects)`)
- [ ] 3.5 Create `UserLookupPort` outbound port interface in project domain (`Optional<UserId> findUserIdByZepUsername(String username)`)

## 4. SyncProjectsUseCase Implementation

- [ ] 4.1 Create `SyncProjectsService` implementing `SyncProjectsUseCase`; constructor-inject `ZepProjectPort` and `ProjectRepository`
- [ ] 4.2 Implement `sync()`: fetch all from ZEP, upsert by zepId (create if absent, `syncFromZep` if present), call `ProjectRepository.saveAll()`

## 5. ReconcileLeadsUseCase Implementation

- [ ] 5.1 Create `ReconcileLeadsService` implementing `ReconcileLeadsUseCase`; constructor-inject `ZepProjectPort`, `ProjectRepository`, `UserLookupPort`, and user domain's `UserRepository`
- [ ] 5.2 Implement `reconcile()`: for each project, fetch lead usernames from ZEP, resolve to UserIds via `UserLookupPort`, call `project.setLeads(resolvedIds)`
- [ ] 5.3 Implement `PROJECT_LEAD` role assignment: collect all UserIds that are leads on any project; add `PROJECT_LEAD` to those users, remove from users no longer leading any project; call `UserRepository.saveAll()`

## 6. Outbound Adapters

- [ ] 6.1 Create `ZepProjectAdapter` implementing `ZepProjectPort`; use existing `com.gepardec.mega.zep.rest.service.ProjectService` for paginated fetch and employee fetch
- [ ] 6.2 Verify which `ZepProjectEmployeeType` id value(s) represent leads (check `ProjectEmployeesMapper` in legacy code); filter accordingly in `fetchLeadUsernames`
- [ ] 6.3 Create `ProjectEntity` JPA entity mapping to `hexagon_projects` table
- [ ] 6.4 Create `ProjectPanacheRepository` extending `PanacheRepository<ProjectEntity>`
- [ ] 6.5 Create `ProjectRepositoryAdapter` implementing `ProjectRepository`; handle `Project ↔ ProjectEntity` mapping; upsert logic in `saveAll`
- [ ] 6.6 Create `UserLookupAdapter` implementing `UserLookupPort`; query `hexagon_users` by `zep_username` column via a Panache query

## 7. Unified SyncScheduler

- [ ] 7.1 Create `SyncScheduler` in `application/schedule/` annotated `@ApplicationScoped`; inject `SyncUsersUseCase`, `SyncProjectsUseCase`, `ReconcileLeadsUseCase`
- [ ] 7.2 Implement `@Scheduled(every = "PT30M", delay = 15, delayUnit = SECONDS)` method that calls the three use cases in sequence
- [ ] 7.3 Delete `hexagon/user/adapter/inbound/UserSyncScheduler.java`

## 8. Tests

- [ ] 8.1 Unit test `SyncProjectsService`: verify upsert-by-zepId logic, new project creation, existing project update, no lead mutation
- [ ] 8.2 Unit test `ReconcileLeadsService`: verify lead resolution, unknown username skipping, full leads-set replacement, `PROJECT_LEAD` role gain and revocation
- [ ] 8.3 Unit test `Project` aggregate: create, reconstitute, syncFromZep, setLeads
- [ ] 8.4 Unit test `ZepProjectAdapter`: verify pagination handling and lead type filtering
