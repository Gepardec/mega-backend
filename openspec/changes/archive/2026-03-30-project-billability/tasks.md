## 1. Domain Model

- [x] 1.1 Add `billable` boolean field to `ZepProjectProfile` record
- [x] 1.2 Add `billable` boolean field to `Project` aggregate; update private constructor
- [x] 1.3 Update `Project.create(ProjectId, ZepProjectProfile)` to populate `billable` from profile
- [x] 1.4 Update `Project.reconstitute(...)` signature and implementation to include `billable`
- [x] 1.5 Update `Project.syncFromZep(ZepProjectProfile)` to update `billable`
- [x] 1.6 Add `getBillable()` (or `isBillable()`) accessor to `Project`

## 2. ZEP Mapping

- [x] 2.1 Add `isBillable(ZepBillingType)` named default method to `ZepProjectMapper` (id 1 or 2 → true, null or other → false)
- [x] 2.2 Add `@Mapping(target = "billable", source = "billingType", qualifiedByName = "isBillable")` to `ZepProjectMapper.toProfile(...)`

## 3. Persistence

- [x] 3.1 Add `billable BOOLEAN NOT NULL DEFAULT false` column to `project` table via new Liquibase changelog
- [x] 3.2 Add `billable` field to `ProjectEntity`
- [x] 3.3 Update `ProjectMapper` (entity↔domain MapStruct mapper) to include `billable`
- [x] 3.4 Update `ProjectRepositoryAdapter.reconstitute(...)` call to pass `billable` from entity

## 4. Tests

- [x] 4.1 Unit test `ZepProjectMapper`: assert billable=true for billing type ids 1 and 2, false for 3/4/null
- [x] 4.2 Unit test `Project`: verify `create`, `reconstitute`, and `syncFromZep` correctly set/update `billable`
