## Context

The hexagonal backend (`com.gepardec.mega.hexagon`) is being built alongside the legacy `com.gepardec.mega` code. The user domain (`hexagon/user/`) is already in place with a `UserSyncScheduler` running every 30 minutes. The legacy `ProjectSyncServiceImpl` mixes two concerns: syncing project master data and generating `ProjectEntry` workflow records. The new hexagonal project sync separates these: only master data is synced; `ProjectEntry` generation is not part of this change.

ZEP exposes two separate REST endpoints relevant here:
- `GET /projects?start=&end=` — paginated list of projects (no lead info)
- `GET /projects/{id}/employees` — per-project employee list with type (employee vs. lead)

## Goals / Non-Goals

**Goals:**
- Sync project master data (name, zepId, startDate, endDate) from ZEP into `hexagon_projects`
- Resolve project leads (ZEP usernames → `UserId` UUIDs) and persist in `hexagon_project_leads`
- Assign `PROJECT_LEAD` role to users who are leads on at least one active project
- Replace `UserSyncScheduler` with a unified `SyncScheduler` that sequences all steps
- Keep each use case independently testable and independently failable

**Non-Goals:**
- Generating `ProjectEntry` workflow records (that is a separate future concern)
- Syncing project employees (non-lead members) — leads only
- Deactivating/archiving projects that no longer appear in ZEP (out of scope for now)
- Scoping the sync to a specific `YearMonth` — we sync all currently active projects

## Decisions

### D1: Three sequential use cases, not one

**Decision:** Split the work into `SyncUsersUseCase`, `SyncProjectsUseCase`, and `ReconcileLeadsUseCase`, executed sequentially by a single `SyncScheduler`.

**Rationale:** Users and projects have a circular reference: project sync needs users to resolve leads; user sync needs projects to assign the `PROJECT_LEAD` role. Running them in sequence with a dedicated reconciliation step breaks the cycle cleanly. Each step can fail independently without corrupting the others.

**Alternative considered:** A single combined sync that fetches users, projects, and leads in one pass. Rejected because it creates a tightly coupled, hard-to-test god service — the same mistake the legacy `ProjectSyncServiceImpl` made.

---

### D2: Project sync fetches all active projects, not scoped by YearMonth

**Decision:** `ZepProjectPort.fetchAll()` fetches all projects with no date filter.

**Rationale:** The legacy sync queries ZEP with `YearMonth.now()`, which means past projects are never updated once the month rolls over. The hexagonal DB is a cache of ZEP truth; it should reflect all active projects, not just the current month's. The ZEP API `start_date`/`end_date` query parameters are optional — omitting them returns all projects.

**Alternative considered:** Keep the `YearMonth.now()` scoping. Rejected because it creates a hole: a project that spans multiple months would only be upserted in the month it's first seen, then never updated.

---

### D3: Reconcile leads as a separate use case with its own ZEP port method

**Decision:** `ReconcileLeadsUseCase` calls `ZepProjectPort.fetchLeadUsernames(zepProjectId)` per project (N calls). It does not reuse data stored during project sync.

**Rationale:** The ZEP project list endpoint returns no lead data. Fetching leads requires a separate per-project call anyway. Storing intermediate lead usernames in the project row to avoid re-fetching would add complexity and a transient state that needs to be cleaned up. Making the reconcile step self-contained and ZEP-authoritative is simpler.

**Risk accepted:** N+1 ZEP API calls per reconcile cycle. Mitigated by the 30-minute interval — this is not a hot path.

---

### D4: `UserLookupPort` defined in the project domain

**Decision:** The project domain defines `outbound.UserLookupPort { Optional<UserId> findUserIdByZepUsername(String username) }`. The adapter queries `hexagon_users` directly via a Panache query. The project domain has no import of any `hexagon/user/` class.

**Rationale:** Clean bounded context separation. The project domain expresses what it needs (a username→id lookup), not how to get it. This keeps the project domain independently compilable and testable.

**Alternative considered:** Have `ReconcileLeadsUseCase` directly inject the user domain's `UserRepository`. Rejected because it creates a compile-time dependency between domains, making future separation impossible.

---

### D5: Unknown leads are skipped silently

**Decision:** If a ZEP lead username cannot be resolved in `hexagon_users`, that lead is skipped. The project is still persisted with the leads that could be resolved. No error is thrown.

**Rationale:** The user sync and project sync run sequentially in the same scheduler invocation, so this should only happen transiently (e.g. a brand-new employee appears in ZEP before the user sync has run). The next 30-minute cycle will resolve it.

---

### D6: Unified `SyncScheduler` replaces `UserSyncScheduler`

**Decision:** A new `SyncScheduler` in `application/schedule/` (outside both domains) injects all three use case ports and sequences them. `UserSyncScheduler` is deleted.

**Rationale:** The scheduler is infrastructure, not domain logic. Keeping it outside both domains prevents either domain from depending on scheduling infrastructure. A single scheduler is also easier to reason about for ops — one identity, one log entry, one failure surface.

## Risks / Trade-offs

**[Risk] ReconcileLeads runs N ZEP API calls per cycle** → Mitigation: 30-minute interval keeps the load low. If ZEP introduces rate limiting, add a small delay between calls or batch the lookup.

**[Risk] If SyncProjects fails, ReconcileLeads is skipped** → This is intentional — reconciling stale project data would be worse. The next cycle retries everything.

**[Risk] Lead FK constraint violations if hexagon_users row is deleted** → Mitigation: `ON DELETE CASCADE` or `ON DELETE SET NULL` on `hexagon_project_leads.user_id`. Given users are only deactivated (not deleted) in the current user sync design, this risk is low.

**[Risk] Project `name` uniqueness constraint** → ZEP project names are treated as the natural key. If ZEP ever renames a project, the upsert-by-name logic will create a duplicate rather than updating. Mitigation: upsert by `zep_id` as the stable key, with `name` as an updatable field. This is the safer approach.

## Migration Plan

1. Add Liquibase changeset: create `hexagon_projects` and `hexagon_project_leads` tables
2. Deploy — new tables exist, old scheduler still runs, no conflict
3. Delete `UserSyncScheduler`, introduce `SyncScheduler` — user sync continues uninterrupted via new scheduler
4. No rollback complexity: the new tables are additive; removing them is the only rollback step needed

## Open Questions

- **Which `ZepProjectEmployeeType` ID values indicate a lead vs. an employee?** The existing `ProjectEmployeesMapper` in the legacy code has this mapping — needs to be verified and carried into `ZepProjectAdapter`.
- **Should projects be scoped to "currently active" (no endDate or endDate ≥ today), or all projects ever?** Current decision is all projects returned by ZEP with no extra filtering — but worth confirming with the team.
- **`PROJECT_LEAD` role lifetime**: if a user is removed as a lead from all projects, should the role be revoked on the next reconcile cycle? The current design re-derives roles on every reconcile run, so yes — but this needs explicit confirmation.
