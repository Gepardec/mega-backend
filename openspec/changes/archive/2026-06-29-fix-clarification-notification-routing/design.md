## Context

Clarification lifecycle notifications are sent by `ClarificationLifecycleNotificationAdapter`, which observes CDI events fired by the month-end application services. The adapter currently routes all `CREATED`, `UPDATED`, and `DELETED` notifications to `subjectEmployeeId`. The domain permits the subject employee to also be the clarification creator, so when that happens the notification is a self-notification — useless and confusing. The eligible project leads should be notified instead.

Events currently in play: `ClarificationCreatedEvent`, `ClarificationUpdatedEvent`, `ClarificationDeletedEvent`, `ClarificationCompletedEvent`. None of the first three carry `eligibleProjectLeadIds`, so the adapter cannot currently distinguish which side the creator is on.

## Goals / Non-Goals

**Goals:**
- Correct recipient routing for `CREATED`, `UPDATED`, and `DELETED` events based on creator identity
- Fan-out to all eligible project leads when they are the intended recipients
- Guard the `COMPLETED` handler against system-actor creators to prevent a `loadUser` failure
- Keep the `COMPLETED` handler's existing logic otherwise unchanged

**Non-Goals:**
- Changing notification content or templates
- Altering the `ZEP`-source suppression rule or project-level skip rule
- Modifying system-created clarification behaviour (`createBySystem` fires no events — unchanged)
- Introducing any new email channel or port

## Decisions

### Enrich events with `eligibleProjectLeadIds` rather than loading from the repository in the adapter

The adapter could fetch the clarification from `MonthEndClarificationRepository` to obtain `eligibleProjectLeadIds` at notification time. This was rejected because it couples the notification BC to the month-end persistence adapter, introduces an extra DB read after the transaction, and violates the principle that event observers should not reach back into the originating BC's persistence layer.

Instead, `ClarificationCreatedEvent`, `ClarificationUpdatedEvent`, and `ClarificationDeletedEvent` are each enriched with a `Set<UserId> eligibleProjectLeadIds` field. The application services already hold this value at event-fire time (the clarification aggregate carries it). The events become self-contained and the adapter stays side-effect-only.

### Fan-out at the adapter level with one `NotificationMailPort.send` call per lead

When eligible leads are the target, the adapter iterates over `eligibleProjectLeadIds` and calls `notificationMailPort.send(...)` once per lead. This is consistent with the existing port contract (one call = one email) and avoids changing the `NotificationMailPort` interface.

### Routing rule: compare creator/actor to `eligibleProjectLeadIds`

For `CREATED` and `DELETED` events the creator field is used. For `UPDATED` the actor field is used (only the creator can edit text, so they are equivalent). The rule:
- creator ∈ eligibleProjectLeadIds → single email to `subjectEmployee`
- creator == subjectEmployee → fan-out to all leads in `eligibleProjectLeadIds`

### System-actor guard on COMPLETED handler

When `createdBy == SystemActor.USER_ID`, attempting `loadUser(SystemActor.USER_ID)` would throw because the system actor has no real user record. The handler will log and return early in this case. In practice, system-created clarifications are auto-created for fully-absent employees and are unlikely to be completed interactively, but the guard prevents a runtime failure if they ever are.

## Risks / Trade-offs

- **Increased email volume when employee creates clarification** — if a project has many eligible leads, each receives their own email. This is the intended behaviour per the decision to fan-out, but bears noting. → No mitigation needed; accepted by design.
- **Event payload grows** — adding `Set<UserId>` to three events increases their size slightly. Negligible in practice since CDI events are in-process. → No mitigation needed.
- **`UpdateMonthEndClarificationService` does not load `eligibleProjectLeadIds`** — currently the service loads the clarification to call `editText`, so `eligibleProjectLeadIds` is available from the aggregate without an extra query. → Verified safe.

## Open Questions

- None. Routing rules, fan-out approach, and system-actor guard are all decided.
