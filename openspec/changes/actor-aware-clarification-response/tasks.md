## 1. Domain Model

- [ ] 1.1 Delete `MonthEndOverviewClarificationItem` record
- [ ] 1.2 Change `MonthEndStatusOverview.clarifications` field type from `List<MonthEndOverviewClarificationItem>` to `List<MonthEndClarification>`

## 2. Application Layer

- [ ] 2.1 Remove clarification item-building logic from `AssembleMonthEndStatusOverviewService` — replace the `MonthEndOverviewClarificationItem` stream with a direct pass-through of the raw `List<MonthEndClarification>` into `MonthEndStatusOverview`

## 3. OpenAPI Spec

- [ ] 3.1 Add `canEditText: boolean` and `canDelete: boolean` fields to `MonthEndOverviewClarificationEntry` in the OpenAPI YAML
- [ ] 3.2 Verify that create, update, and complete clarification response schemas reference the same enriched clarification entry shape (or align them if they differ)

## 4. REST Adapter

- [ ] 4.1 Add clarification enrichment method to the REST mapper: accepts `MonthEndClarification`, `Map<UserId, UserRef>`, and `UserId actorId`; maps to the enriched DTO with UserRefs for `createdBy`, `subjectEmployee`, and `resolvedBy`, plus `canResolve`, `canEditText`, and `canDelete` flags
- [ ] 4.2 Update the overview REST adapter (employee and lead): inject `MonthEndUserSnapshotPort`, collect all UserIds referenced by the clarification list, call `findByIds()`, build `Map<UserId, UserRef>`, and use the new enrichment method when mapping clarification entries
- [ ] 4.3 Update the create clarification REST adapter (employee and lead resources): inject `MonthEndUserSnapshotPort`, enrich and return the created clarification using the new mapper method
- [ ] 4.4 Update the update clarification REST adapter: inject `MonthEndUserSnapshotPort`, enrich and return the updated clarification using the new mapper method
- [ ] 4.5 Update the complete clarification REST adapter: inject `MonthEndUserSnapshotPort`, enrich and return the resolved clarification using the new mapper method

## 5. Tests

- [ ] 5.1 Update `AssembleMonthEndStatusOverviewService` tests — remove assertions on `MonthEndOverviewClarificationItem` fields; assert that raw `MonthEndClarification` instances are passed through unchanged
- [ ] 5.2 Add REST mapper tests for the clarification enrichment method — cover UserRef resolution, `canResolve`, `canEditText`, and `canDelete` flag evaluation for all permission combinations (creator, non-creator involved, non-involved)
- [ ] 5.3 Update REST resource tests — assert `canEditText` and `canDelete` are present in overview and mutation responses
