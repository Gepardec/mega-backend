## 1. Promote Shared Identity Types to Shared Kernel

- [ ] 1.1 Move `UserId` from `hexagon.user.domain.model` to `hexagon.shared.domain.model` and update all import references in `src/main` and `src/test`
- [ ] 1.2 Move `Email` from `hexagon.user.domain.model` to `hexagon.shared.domain.model` and update all import references in `src/main` and `src/test`
- [ ] 1.3 Move `ProjectId` from `hexagon.project.domain.model` to `hexagon.shared.domain.model` and update all import references in `src/main` and `src/test`

## 2. Move Inbound Port Interfaces to Application Layer

- [ ] 2.1 Move all 9 `*UseCase` interfaces from `hexagon.monthend.domain.port.inbound` to `hexagon.monthend.application` and update all import references in `src/main` and `src/test`
- [ ] 2.2 Move both `*UseCase` interfaces from `hexagon.worktime.domain.port.inbound` to `hexagon.worktime.application` and update all import references in `src/main` and `src/test`
- [ ] 2.3 Move both `*UseCase` interfaces from `hexagon.project.domain.port.inbound` to `hexagon.project.application` and update all import references in `src/main` and `src/test`
- [ ] 2.4 Move `SyncUsersUseCase` from `hexagon.user.domain.port.inbound` to `hexagon.user.application` and update all import references in `src/main` and `src/test`
- [ ] 2.5 Delete the now-empty `domain/port/inbound` packages from all four modules

## 3. Reclassify MonthEndTaskPlanningService as Domain Service

- [ ] 3.1 Move `MonthEndTaskPlanningService` from `hexagon.monthend.application` to `hexagon.monthend.domain.services` and update all import references in `src/main` and `src/test`

## 4. Reclassify SyncScheduler as Inbound Adapter

- [ ] 4.1 Move `SyncScheduler` from `hexagon.application.schedule` to `hexagon.shared.adapter.inbound` and update all import references in `src/main` and `src/test`
- [ ] 4.2 Delete the now-empty `application/schedule` package

## 5. Update Architecture Tests

- [ ] 5.1 Update all `ArchitectureTest.java` package path strings that reference `domain.port.inbound` to reference `application` instead

## 6. Verify

- [ ] 6.1 Run `mvn clean package` and confirm a clean build with all tests passing
