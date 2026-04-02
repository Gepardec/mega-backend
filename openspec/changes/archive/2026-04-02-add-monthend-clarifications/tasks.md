## 1. Domain and Persistence

- [x] 1.1 Add the `MonthEndClarification` domain model, identifiers, enums, and repository port with rules for creation, creator-side editing, opposite-side completion, and timestamp updates
- [x] 1.2 Add Liquibase changelogs, JPA entities, MapStruct mappers, and the persistence adapter for clarifications and their eligible lead snapshot

## 2. Application and Worklists

- [x] 2.1 Implement create, update, and complete clarification use cases/services, including actor authorization and optional resolution-note handling
- [x] 2.2 Extend the month-end worklist read model and query services so employee and lead worklists return visible open clarifications alongside generated tasks

## 3. Verification

- [x] 3.1 Add unit tests for clarification aggregate rules, including creator-side edits, resolver-side completion, ineligible actor rejection, and immutable done-state behaviour
- [x] 3.2 Add repository and integration tests for clarification visibility, lead-snapshot persistence, worklist composition, and confirmation that open clarifications do not block generated task completion
