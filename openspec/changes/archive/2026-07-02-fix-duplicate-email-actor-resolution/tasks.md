## 1. Auth Guard

- [x] 1.1 In `UserRepositoryAdapter.findByEmail`, replace `firstResultOptional()` with a list query; if the result contains more than one element, throw an `IllegalStateException` (or a domain-appropriate exception) with a message indicating an ambiguous email match — never return a result silently

## 2. Sync: Returning Employee Email Fallback

- [x] 2.1 In `SyncUsersService.findExistingUsers`, after the batch `findByZepUsernames` lookup, identify any ZEP employees from the input whose `zepUsername` was not found in the batch result
- [x] 2.2 For each unmatched employee, call `userRepository.findByEmail(employee.email())` and, if a `User` is found, add it to the existing-users map keyed by the employee's `zepUsername`
- [x] 2.3 Verify that `synchronizeUser` already correctly calls `existingUser.withSyncedZepData(zepEmployee, roles)` for matched users — this updates `zepUsername` and `employmentPeriods` in place, which is the correct behaviour for returning employees; no change needed if it does

## 3. Database Constraint

- [x] 3.1 Add a new Liquibase changeset under `src/main/resources/db/changelog/hexagon/` that adds a `UNIQUE` constraint on the `users.email` column
- [x] 3.2 Add `unique = true` to the `@Column(name = "email")` annotation on `UserEntity.email`

## 4. Tests

- [x] 4.1 Unit test for `UserRepositoryAdapter.findByEmail`: when the query returns two or more results, the method throws rather than returning a value
- [x] 4.2 Unit test for `SyncUsersService`: when a ZEP employee is not found by `zepUsername` but a `User` exists with the same email, the existing User is updated (not a new one created) and the returned `UserSyncResult.updated` count reflects it
- [x] 4.3 Unit test for `SyncUsersService`: when a ZEP employee is not found by `zepUsername` and no `User` exists with that email, a new User is created as before
