## 1. Enrich domain events with eligible project leads

- [x] 1.1 Add `Set<UserId> eligibleProjectLeadIds` field to `ClarificationCreatedEvent`
- [x] 1.2 Add `Set<UserId> eligibleProjectLeadIds` field to `ClarificationUpdatedEvent`
- [x] 1.3 Add `Set<UserId> eligibleProjectLeadIds` field to `ClarificationDeletedEvent`

## 2. Update application services to include eligible leads when firing events

- [x] 2.1 Update `CreateMonthEndClarificationService` to pass `clarification.eligibleProjectLeadIds()` in `ClarificationCreatedEvent`
- [x] 2.2 Update `UpdateMonthEndClarificationService` to pass `updatedClarification.eligibleProjectLeadIds()` in `ClarificationUpdatedEvent`
- [x] 2.3 Update `DeleteMonthEndClarificationService` to pass `clarification.eligibleProjectLeadIds()` in `ClarificationDeletedEvent`

## 3. Fix notification routing in the adapter

- [x] 3.1 Update `onClarificationCreated`: if creator ∈ eligibleProjectLeadIds → send to subject employee; if creator == subject employee → fan out to all eligible leads
- [x] 3.2 Update `onClarificationUpdated`: same routing logic using `actorId` instead of creator
- [x] 3.3 Update `onClarificationDeleted`: same routing logic using creator
- [x] 3.4 Add system-actor guard to `onClarificationCompleted`: if `creator == SystemActor.USER_ID` log and return early

## 4. Update tests

- [x] 4.1 Update `ClarificationCreatedEvent`, `ClarificationUpdatedEvent`, `ClarificationDeletedEvent` test usages to include `eligibleProjectLeadIds`
- [x] 4.2 Update `CreateMonthEndClarificationServiceTest` to assert `eligibleProjectLeadIds` is included in fired event
- [x] 4.3 Update `UpdateMonthEndClarificationServiceTest` to assert `eligibleProjectLeadIds` is included in fired event
- [x] 4.4 Update `DeleteMonthEndClarificationServiceTest` (if exists) to assert `eligibleProjectLeadIds` is included in fired event
- [x] 4.5 Update `ClarificationLifecycleNotificationAdapterTest`: add scenario where lead creates → subject employee notified
- [x] 4.6 Update `ClarificationLifecycleNotificationAdapterTest`: add scenario where subject employee creates → all eligible leads notified (fan-out)
- [x] 4.7 Update `ClarificationLifecycleNotificationAdapterTest`: add same two scenarios for `UPDATED` and `DELETED`
- [x] 4.8 Update `ClarificationLifecycleNotificationAdapterTest`: add scenario where `COMPLETED` with system-actor creator is skipped
