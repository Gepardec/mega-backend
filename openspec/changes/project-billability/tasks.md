## 1. Domain Model

- [ ] 1.1 Add `billable` boolean field to `ZepProjectProfile` record
- [ ] 1.2 Add `billable` boolean field to `Project` aggregate; update private constructor
- [ ] 1.3 Update `Project.create(ProjectId, ZepProjectProfile)` to populate `billable` from profile
- [ ] 1.4 Update `Project.reconstitute(...)` signature and implementation to include `billable`
- [ ] 1.5 Update `Project.syncFromZep(ZepProjectProfile)` to update `billable`
- [ ] 1.6 Add `getBillable()` (or `isBillable()`) accessor to `Project`

## 2. ZEP Mapping

- [ ] 2.1 Add `isBillable(ZepBillingType)` named default method to `ZepProjectMapper` (id 1 or 2 → true, null or other → false)
- [ ] 2.2 Add `@Mapping(target = "billable", source = "billingType", qualifiedByName = "isBillable")` to `ZepProjectMapper.toProfile(...)`

## 3. Persistence

- [ ] 3.1 Add `billable BOOLEAN NOT NULL DEFAULT false` column to `project` table via new Liquibase changelog
- [ ] 3.2 Add `billable` field to `ProjectEntity`
- [ ] 3.3 Update `ProjectMapper` (entity↔domain MapStruct mapper) to include `billable`
- [ ] 3.4 Update `ProjectRepositoryAdapter.reconstitute(...)` call to pass `billable` from entity

## 4. Tests

- [ ] 4.1 Unit test `ZepProjectMapper`: assert billable=true for billing type ids 1 and 2, false for 3/4/null
- [ ] 4.2 Unit test `Project`: verify `create`, `reconstitute`, and `syncFromZep` correctly set/update `billable`
