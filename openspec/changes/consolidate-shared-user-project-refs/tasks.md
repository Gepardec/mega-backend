## 1. Move value objects to shared kernel

- [ ] 1.1 Move `FullName` from `user/domain/model/` to `shared/domain/model/`; update all imports (compiler-guided)
- [ ] 1.2 Move `ZepUsername` from `user/domain/model/` to `shared/domain/model/`; update all imports (compiler-guided)

## 2. Add shared reference types

- [ ] 2.1 Add `UserRef { UserId id, FullName fullName, ZepUsername zepUsername }` to `shared/domain/model/`
- [ ] 2.2 Add `ProjectRef { ProjectId id, int zepId, String name }` to `shared/domain/model/`

## 3. Update port interfaces

- [ ] 3.1 Change `MonthEndUserSnapshotPort`: rename `findAll()` to `findActiveIn(YearMonth month)`, return type `List<UserRef>`
- [ ] 3.2 Change `MonthEndProjectSnapshotPort`: rename `findAll()` to `findActiveIn(YearMonth month)`, return type `List<MonthEndProjectSnapshot>`
- [ ] 3.3 Change worktime user snapshot port: rename `findAll()` to `findActiveIn(YearMonth month)`, return type `List<UserRef>`
- [ ] 3.4 Change worktime project snapshot port: rename `findAll()` to `findActiveIn(YearMonth month)`, return type `List<ProjectRef>`

## 4. Update adapter implementations

- [ ] 4.1 Update `UserSnapshotAdapter` (monthend): implement `findActiveIn(YearMonth)` — push employment-period activeness filter into the adapter
- [ ] 4.2 Update project snapshot adapter (monthend): implement `findActiveIn(YearMonth)` — push date-range activeness filter into the adapter; return `MonthEndProjectSnapshot` without `startDate`/`endDate`
- [ ] 4.3 Update worktime user snapshot adapter: implement `findActiveIn(YearMonth)` — reuse employment-period activeness logic
- [ ] 4.4 Update worktime project snapshot adapter: implement `findActiveIn(YearMonth)`; return `ProjectRef`

## 5. Trim MonthEndProjectSnapshot

- [ ] 5.1 Remove `startDate`, `endDate` fields from `MonthEndProjectSnapshot`
- [ ] 5.2 Remove `isActiveIn(YearMonth)` method from `MonthEndProjectSnapshot`
- [ ] 5.3 Fix any compilation errors caused by removed fields/method

## 6. Delete redundant types and replace usages

- [ ] 6.1 Delete `MonthEndUserSnapshot`; replace all usages with `UserRef`
- [ ] 6.2 Delete `MonthEndEmployee`; replace all usages with `UserRef`
- [ ] 6.3 Delete `WorkTimeUserSnapshot`; replace all usages with `UserRef`
- [ ] 6.4 Delete `WorkTimeEmployee`; replace all usages with `UserRef`
- [ ] 6.5 Delete `MonthEndProject`; replace all usages with `ProjectRef`
- [ ] 6.6 Delete `WorkTimeProject`; replace all usages with `ProjectRef`
- [ ] 6.7 Delete `WorkTimeProjectSnapshot`; replace all usages with `ProjectRef`

## 7. Update application services

- [ ] 7.1 Update `GenerateMonthEndTasksService`: replace `findAll().stream().filter(user -> user.isActiveIn(month))` with `findActiveIn(month)` port call; remove project activeness filter similarly
- [ ] 7.2 Update any other monthend or worktime application service that calls `findAll()` on user/project snapshot ports

## 8. Simplify mappers

- [ ] 8.1 Remove `MonthEndWorklistMapper.toProject()` and `toSubjectEmployee()` projection methods
- [ ] 8.2 Update `MonthEndWorklistItem` field types to `UserRef` and `MonthEndProjectSnapshot` directly
- [ ] 8.3 Update `MonthEndStatusOverviewItem` field types similarly if affected
- [ ] 8.4 Update any worktime mappers that previously mapped to deleted types

## 9. Verify

- [ ] 9.1 Run `mvn test -Dtest=ArchitectureTest` — all architecture rules pass
- [ ] 9.2 Run `mvn test` — full test suite passes with no regressions
