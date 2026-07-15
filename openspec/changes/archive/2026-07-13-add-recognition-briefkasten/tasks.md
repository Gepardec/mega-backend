## 1. Domain

- [x] 1.1 Create the `recognition` bounded-context package skeleton (`domain`, `application`, `adapter/inbound`, `adapter/outbound`) under `com.gepardec.mega.hexagon.recognition`
- [x] 1.2 Add the `RecognitionCategory` domain type with values for praise/appreciation and courage
- [x] 1.3 Add the recognition entry status type with values `NEW` and `INCLUDED_IN_DIGEST`
- [x] 1.4 Implement the `RecognitionEntry` aggregate: identity, non-empty message, category, submission timestamp, status; reject blank message; provide the one-directional transition from `NEW` to `INCLUDED_IN_DIGEST`; no edit/delete behavior
- [x] 1.5 Add the mail-recipient value type (email + first name) used by the digest, owned by the recognition BC

## 2. Persistence

- [x] 2.1 Add a Liquibase changelog creating the `recognition_entry` table (message, category, submitted_at, status) and wire it into the changelog master
- [x] 2.2 Add the recognition entry JPA entity and Panache repository
- [x] 2.3 Add the outbound repository port and its adapter, with a mapper between entity and aggregate; support persisting a new entry, finding entries by status `NEW`, and saving status transitions

## 3. Recipient resolution (cross-BC)

- [x] 3.1 Define the outbound `ProjectLeadDirectoryPort` returning the active internal project-lead recipients for a supplied `LocalDate`
- [x] 3.2 Implement its adapter in `recognition/adapter/outbound`: query the `user` BC for project-lead-role users, filter to active-on-date and internal (non-external), and map each to the mail-recipient value
- [x] 3.3 Unit-test the adapter's filtering (active/inactive, internal/external, non-lead) for arbitrary dates

## 4. Submission use case + REST

- [x] 4.1 Add the inbound submit use case and application service that validates and persists a new entry with status `NEW`, stamping `submittedAt` from an injected `Clock` (not the no-arg `now()`)
- [x] 4.2 Add the REST resource with a submit endpoint, request DTO (`message`, `category`), and mapping to the use case
- [x] 4.3 Secure the endpoint to require authentication and the `EMPLOYEE` role, and reject external employees (only internal employees may submit)
- [x] 4.4 Add REST tests: successful submit, blank message rejected, invalid category rejected, unauthenticated rejected, missing-role rejected, external employee rejected

## 5. Weekly digest use case

- [x] 5.1 Add the inbound digest use case that derives its reference date via `LocalDate.now(clock)` from an injected `Clock`, resolves recipients via `ProjectLeadDirectoryPort` for that date, and gathers entries with status `NEW`
- [x] 5.2 Send the digest to every recipient (grouping/labeling entries by category), including an empty-state message when there are no new entries; send nothing when there are no recipients
- [x] 5.3 Transition sent entries to `INCLUDED_IN_DIGEST` only after the send loop completes, so a failed run leaves them `NEW`
- [x] 5.4 Unit-test the use case with a fixed injected `Clock`: new entries included and marked, empty-state sent when no entries, no-op when no recipients, failure leaves entries `NEW`

## 6. Mail adapter

- [x] 6.1 Define the outbound `RecognitionMailPort` speaking the digest's language (send a digest of entries to a recipient)
- [x] 6.2 Implement its adapter reusing the Quarkus mailer, subject-prefix config, and inline-logo conventions
- [x] 6.3 Add the digest HTML email template under `emails/` and the subject message key
- [x] 6.4 Test the adapter renders the entry list and the empty-state variant and sends to the recipient address

## 7. Scheduling

- [x] 7.1 Add the inbound weekly `@Scheduled` adapter (cron `0 0 17 ? * MON` — Monday 17:00 Redaktionsschluss) that invokes the digest use case; keep it thin (no date computation — the service derives the date from the injected `Clock`); log start and outcome
- [x] 7.2 Ensure the scheduler is disabled under the test profile, consistent with existing scheduled jobs

## 8. Architecture checks

- [x] 8.1 Confirm the recognition domain and application layers do not depend on the `user` aggregate (only the outbound adapter reaches into `user`), and that architecture tests pass
