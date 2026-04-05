## 1. User Snapshot Lookup

- [ ] 1.1 Extend the month-end user snapshot read model with a display-ready `fullName` and add a targeted `findByIds` operation on `MonthEndUserSnapshotPort` for requested user ids.
- [ ] 1.2 Update the user snapshot adapter and supporting user mapping/query code to return enriched snapshots for targeted id lookups while preserving the existing `findAll()` behavior.

## 2. Status Overview Enrichment

- [ ] 2.1 Replace the flat overview `subjectEmployeeId` field with a nullable nested subject employee reference object in the month-end status overview read model and application mapper.
- [ ] 2.2 Update `GetMonthEndStatusOverviewService` to collect distinct non-null subject employee ids, resolve user snapshots in bulk, and map the nested subject employee reference into each overview entry.
- [ ] 2.3 Update application-level mapper and service tests for enriched overview entries, including employee-linked tasks and `ABRECHNUNG` tasks without a subject employee object.

## 3. REST Contract And Verification

- [ ] 3.1 Replace the flat status overview `subjectEmployeeId` in the canonical month-end OpenAPI document with a nullable nested subject employee reference schema and regenerate the REST models.
- [ ] 3.2 Update the month-end REST mapper and shared resource expectations so the nested subject employee object is returned unchanged through the REST boundary.
- [ ] 3.3 Update REST mapper, resource, and integration tests to verify subject employee id and full name for employee-linked overview entries and omission for `ABRECHNUNG` entries.
