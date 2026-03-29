## 1. UserMapper

- [ ] 1.1 Create `UserMapper` abstract class in `user/adapter/outbound/` annotated with `@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)`
- [ ] 1.2 Add abstract `toEntity(User user)` method with `@Mapping` annotations for `id.value`, `email.value`, `name.firstname`, `name.lastname`, `status`, `roles`
- [ ] 1.3 Add concrete `toDomain(UserEntity entity)` method that calls `User.reconstitute()` with inline value object construction (`UserId.of()`, `Email.of()`, `FullName.of()`)
- [ ] 1.4 Add named helper methods for `Set<String> → Set<Role>` and `Set<Role> → Set<String>` enum conversion
- [ ] 1.5 Inject `UserMapper` into `UserRepositoryAdapter` and replace `toDomain()` / `toEntity()` private methods with mapper calls

## 2. ProjectMapper

- [ ] 2.1 Create `ProjectMapper` abstract class in `project/adapter/outbound/` annotated with `@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)`
- [ ] 2.2 Add abstract `toEntity(Project project)` method with `@Mapping` annotations for `id.value`, `zepId`, `name`, `startDate`, `endDate`, `leads`
- [ ] 2.3 Add concrete `toDomain(ProjectEntity entity)` method that calls `Project.reconstitute()` with `ProjectId.of()` inline
- [ ] 2.4 Inject `ProjectMapper` into `ProjectRepositoryAdapter` and replace `toDomain()` / `toEntity()` private methods with mapper calls

## 3. ZepProjectMapper

- [ ] 3.1 Create `ZepProjectMapper` interface (or abstract class) in `project/adapter/outbound/` annotated with `@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)`
- [ ] 3.2 Add `toProfile(ZepProject zepProject) → ZepProjectProfile` method with `@Mapping` annotations; add null-safe `java.sql.Date/Timestamp → LocalDate` conversion helper
- [ ] 3.3 Inject `ZepProjectMapper` into `ZepProjectAdapter` and replace the private `toProfile()` method with mapper call

## 4. ZepEmployeeMapper

- [ ] 4.1 Create `ZepEmployeeMapper` abstract class in `user/adapter/outbound/` annotated with `@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)`
- [ ] 4.2 Add multi-source `toZepProfile(ZepEmployee employee, EmploymentPeriods employmentPeriods, RegularWorkingTimes regularWorkingTimes) → ZepProfile` method
- [ ] 4.3 Add `@Mapping` annotations for direct string fields (`username`, `email`, `firstname`, `lastname`, `title`, `releaseDate`) and null-safe enum expressions for `salutation` and `language`
- [ ] 4.4 Inject `ZepEmployeeMapper` into `ZepEmployeeAdapter`; refactor `toZepProfile()` to build `EmploymentPeriods` and `RegularWorkingTimes` first (keeping `toRegularWorkingTime()` as a private method), then delegate to mapper

## 5. Verification

- [ ] 5.1 Run `mvn test` and confirm all tests pass
- [ ] 5.2 Confirm no compilation warnings from MapStruct about unmapped properties
