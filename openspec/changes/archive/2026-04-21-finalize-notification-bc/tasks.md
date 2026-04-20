## 1. Rename Email Resources

- [x] 1.1 Rename `src/main/resources/emails/COMMENT_CREATED.html` → `CLARIFICATION_CREATED.html`
- [x] 1.2 Rename `src/main/resources/emails/COMMENT_MODIFIED.html` → `CLARIFICATION_UPDATED.html`
- [x] 1.3 Rename `src/main/resources/emails/COMMENT_CLOSED.html` → `CLARIFICATION_COMPLETED.html`
- [x] 1.4 Rename `src/main/resources/emails/COMMENT_DELETED.html` → `CLARIFICATION_DELETED.html`
- [x] 1.5 Rename `src/main/resources/emails/ZEP_COMMENT_PROCESSING_ERROR.html` → `ZEP_CLARIFICATION_PROCESSING_ERROR.html`
- [x] 1.6 Rename corresponding keys in `messages.properties` (`mail.COMMENT_*.subject` → `mail.CLARIFICATION_*.subject`, `mail.ZEP_COMMENT_PROCESSING_ERROR.subject` → `mail.ZEP_CLARIFICATION_PROCESSING_ERROR.subject`)
- [x] 1.7 Rename corresponding keys in `messages_en.properties`

## 2. New Domain Model — Sealed Interface & Enum

- [x] 2.1 Create `MailNotificationId` sealed interface in `hexagon/notification/domain/model/` with `String name()` and `permits ReminderType, ClarificationNotificationType`
- [x] 2.2 Add `implements MailNotificationId` to `ReminderType`
- [x] 2.3 Create `ClarificationNotificationType` enum in `hexagon/notification/domain/model/` implementing `MailNotificationId` with values: `CLARIFICATION_CREATED`, `CLARIFICATION_UPDATED`, `CLARIFICATION_COMPLETED`, `CLARIFICATION_DELETED`, `ZEP_CLARIFICATION_PROCESSING_ERROR`

## 3. New Domain Event — ClarificationUpdatedEvent

- [x] 3.1 Create `ClarificationUpdatedEvent` record in `hexagon/monthend/domain/event/` with fields: `MonthEndClarificationId clarificationId`, `UserId actorId`, `UserId subjectEmployeeId`, `String text`

## 4. Update NotificationMailPort Signature

- [x] 4.1 Change `NotificationMailPort.send()` first parameter from `String mailTypeId` to `MailNotificationId mailId` (both the default and the full overload)

## 5. Rewrite QuarkusMailNotificationAdapter

- [x] 5.1 Rewrite `QuarkusMailNotificationAdapter` to own all template-loading and rendering logic (no imports from `com.gepardec.mega.notification.mail`)
- [x] 5.2 Implement convention-based template resolution: `emails/{id.name()}.html` for template, `mail.{id.name()}.subject` from resource bundle for subject
- [x] 5.3 Implement two-pass render for `ReminderType`: load snippet, inject as `$mailText$` into `emails/reminder-template.html`, substitute remaining parameters
- [x] 5.4 Implement single-pass render for `ClarificationNotificationType`: load template, substitute parameters directly
- [x] 5.5 Inline logo loading and mail dispatch (was in `MailSender`) using Quarkus `Mailer`

## 6. Update All NotificationMailPort Call Sites

- [x] 6.1 Update `ClarificationLifecycleNotificationAdapter` — replace `Mail.COMMENT_*.name()` string calls with `ClarificationNotificationType.*` enum values
- [x] 6.2 Update `SendScheduledRemindersService` — replace `reminderType.name()` with `reminderType` (direct `MailNotificationId` pass-through)
- [x] 6.3 Update `ProcessZepMailService` — replace string constant `"ZEP_COMMENT_PROCESSING_ERROR"` with `ClarificationNotificationType.ZEP_CLARIFICATION_PROCESSING_ERROR`

## 7. Wire ClarificationUpdatedEvent

- [x] 7.1 Inject CDI `Event<ClarificationUpdatedEvent>` into `UpdateMonthEndClarificationService`
- [x] 7.2 Fire `ClarificationUpdatedEvent` in `updateText()` after `monthEndClarificationRepository.save()` succeeds, carrying `clarificationId`, `actorId`, `subjectEmployeeId`, and updated `text`
- [x] 7.3 Add `onClarificationUpdated(@Observes ClarificationUpdatedEvent)` handler to `ClarificationLifecycleNotificationAdapter` — skip if `subjectEmployeeId` is null (project-level), otherwise send `CLARIFICATION_UPDATED` mail to subject employee

## 8. Verify & Clean Up

- [x] 8.1 Confirm no class in `com.gepardec.mega.hexagon` imports anything from `com.gepardec.mega.notification.mail` (grep check)
- [x] 8.2 Run full test suite (`mvn test`) and confirm all tests pass
