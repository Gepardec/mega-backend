## 1. User persistence and aggregate reshape

- [ ] 1.1 Add Liquibase changes for the lean `hexagon_users` model, `hexagon_user_roles`, `hexagon_user_employment_periods`, and nullable `personio_id`, including backfill from existing JSON columns
- [ ] 1.2 Refactor the hexagon `User` aggregate to the new immutable record-oriented shape with direct provider references and employment-period-based activity checks
- [ ] 1.3 Update user persistence adapters and MapStruct mappers to read and write the normalized schema instead of serialized profile/status columns

## 2. User sync and provider boundaries

- [ ] 2.1 Convert user sync into an injected CDI-managed application service with transaction and logging boundaries aligned to the other hexagon domains
- [ ] 2.2 Update sync behavior to process all ZEP employees with email, persist full employment history, assign `OFFICE_MANAGEMENT` by email from the existing config property, and return the richer operation summary
- [ ] 2.3 Change Personio sync handling to resolve and store only missing `personioId` values and stop persisting Personio detail snapshots
- [ ] 2.4 Introduce on-demand provider-detail ports/adapters for Personio-owned detail and ZEP regular working times

## 3. Downstream consumers and cleanup

- [ ] 3.1 Update monthend and worktime adapters/services to consume direct user fields or explicit projections instead of `zepProfile` / `personioProfile`
- [ ] 3.2 Rewrite and extend unit/integration tests for the new aggregate shape, normalized persistence, sync semantics, and provider-detail lookups
- [ ] 3.3 Remove obsolete status/profile persistence columns and compatibility code after the new path is verified
