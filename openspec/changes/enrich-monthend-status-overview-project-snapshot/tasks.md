## 1. Project Snapshot Lookup

- [ ] 1.1 Extend the month-end project snapshot read model with the project name and add a bulk lookup operation on `MonthEndProjectSnapshotPort` for a requested set of project ids.
- [ ] 1.2 Update the project snapshot adapter and supporting project mapping/query code to return the enriched snapshots for targeted project id lookups.

## 2. Status Overview Enrichment

- [ ] 2.1 Extend the month-end status overview entry model to carry the additive project display field while keeping the existing project identifier.
- [ ] 2.2 Update `GetMonthEndStatusOverviewService` to collect distinct project ids from actor tasks, resolve project snapshots in bulk, and map the snapshot name into each overview entry.
- [ ] 2.3 Update application-level mapper and service tests for enriched overview entries, including empty and mixed-status cases.

## 3. REST Contract And Verification

- [ ] 3.1 Extend the canonical month-end OpenAPI status overview entry schema and generated models with the additive project name field.
- [ ] 3.2 Update the month-end REST mapper and shared resource expectations so the enriched overview entry is returned unchanged through the REST boundary.
- [ ] 3.3 Update REST and integration tests to verify shared status overview responses include both `projectId` and `projectName`.
