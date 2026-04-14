## 1. Promote Shared Identity Types to Shared Kernel

- [x] 1.1 Move `UserId` from `hexagon.user.domain.model` to `hexagon.shared.domain.model` and update all import references in `src/main` and `src/test`
- [x] 1.2 Move `Email` from `hexagon.user.domain.model` to `hexagon.shared.domain.model` and update all import references in `src/main` and `src/test`
- [x] 1.3 Move `ProjectId` from `hexagon.project.domain.model` to `hexagon.shared.domain.model` and update all import references in `src/main` and `src/test`

## 2. Move Inbound Port Interfaces to Application Layer

- [x] 2.1 Move all 9 `*UseCase` interfaces from `hexagon.monthend.domain.port.inbound` to `hexagon.monthend.application.port.inbound` and update all import references in `src/main` and `src/test`
- [x] 2.2 Move both `*UseCase` interfaces from `hexagon.worktime.domain.port.inbound` to `hexagon.worktime.application.port.inbound` and update all import references in `src/main` and `src/test`
- [x] 2.3 Move both `*UseCase` interfaces and both sync result records from `hexagon.project.domain.port.inbound` to `hexagon.project.application.port.inbound` and update all import references in `src/main` and `src/test`
- [x] 2.4 Move `SyncUsersUseCase` and `UserSyncResult` from `hexagon.user.domain.port.inbound` to `hexagon.user.application.port.inbound` and update all import references in `src/main` and `src/test`
- [x] 2.5 Delete the now-empty `domain/port/inbound` packages from all four modules

## 3. Reclassify MonthEndTaskPlanningService as Domain Service

- [x] 3.1 Move `MonthEndTaskPlanningService` from `hexagon.monthend.application` to `hexagon.monthend.domain.services` and update all import references in `src/main` and `src/test`

## 4. Reclassify SyncScheduler as Inbound Adapter

- [x] 4.1 Move `SyncScheduler` from `hexagon.application.schedule` to `hexagon.shared.adapter.inbound` and update all import references in `src/main` and `src/test`
- [x] 4.2 Delete the now-empty `application/schedule` package

## 5. Update Architecture Tests

- [x] 5.1 Update all `ArchitectureTest.java` package path strings that reference `domain.port.inbound` to reference `application.port.inbound` instead

## 6. Verify

- [ ] 6.1 Run `mvn clean package` and confirm a clean build with all tests passing
