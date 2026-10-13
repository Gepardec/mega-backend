# Tasks

## 1. Read-only fan-out use cases (already applied)

- [x] 1.1 Annotate `GetProjectLeadWorkTimeService` with `@Transactional(TxType.NEVER)` plus the explanatory comment; verify `GetProjectLeadWorkTimeServiceTest` and `HexagonalArchitectureTest` pass
- [x] 1.2 Annotate `GetEmployeeWorkTimeService` with `@Transactional(TxType.NEVER)` plus the explanatory comment; verify `GetEmployeeWorkTimeServiceTest` passes
- [x] 1.3 Annotate `UpdateInternalRatesService` with `@Transactional(TxType.NEVER)` plus the explanatory comment; verify `UpdateInternalRatesServiceTest` passes

## 2. Per-user release-date persistence

- [x] 2.1 Add `ReleaseDatePersister` (`@ApplicationScoped`) in `hexagon/user/application` with a single-user save method annotated `@Transactional(TxType.REQUIRES_NEW)` that delegates to `UserRepository.saveAll(List.of(user))`; add a unit test (Mockito) verifying the delegation, and verify `HexagonalArchitectureTest` still passes

## 3. `UpdateReleaseDatesService`

- [x] 3.1 Resolve all command users up front via `UserRepository.findByIds` on the calling thread; mark unknown users and users without a ZEP username as failed before the fan-out; pass the resolved `User` into `updateInZep` so the fan-out does no repository access. Verify with unit tests: unknown user is reported failed, `findById` is no longer called, and `findByIds` is called once with all command ids
- [x] 3.2 Replace `userRepository.saveAll` in `persistReleaseDate` with `ReleaseDatePersister`, keeping the per-user `try/catch (RuntimeException)` at the call site; verify with a unit test that a save failure for one user puts only that user into `failedUserIds` while others are still saved
- [x] 3.3 Switch the class to `@Transactional(TxType.NEVER)` with the explanatory comment; verify `UpdateReleaseDatesServiceTest` and `HexagonalArchitectureTest` pass

## 4. `AutoUpdateReleaseDatesService`

- [x] 4.1 Replace `userRepository.saveAll` in `persistReleaseDate` with `ReleaseDatePersister`, keeping the per-user `try/catch` at the call site; verify with a unit test that a save failure for one user lands in `failedUserIds` while others land in `updatedUserIds`
- [x] 4.2 Switch the class to `@Transactional(TxType.NEVER)` with the explanatory comment; verify `AutoUpdateReleaseDatesServiceTest` and `HexagonalArchitectureTest` pass

## 5. Integration check

- [x] 5.1 Run the full test suite (`mvn test`) and verify it passes, including `@QuarkusTest` REST tests for `PUT /users/release-dates` and the worktime endpoints (they exercise `TxType.NEVER` under a real CDI container and H2)
- [x] 5.2 Start the app in dev mode, call `GET` on the project-lead worktime report for a project lead with several projects, and verify no `ARJUNA012107`/`ARJUNA016039` warnings appear in the log
