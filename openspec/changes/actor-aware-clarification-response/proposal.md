## Why

Every clarification mutation (create, update, complete) currently forces a full overview re-fetch on the client because the mutation response does not carry the same enriched shape. Additionally, `MonthEndOverviewClarificationItem` is a presentation model with `UserRef` fields living in the domain layer — a layer violation that mixes display concerns into the domain. The overview DTO also only exposes `canResolve` but the client needs `canEditText` and `canDelete` to render action buttons correctly.

## What Changes

- Remove `MonthEndOverviewClarificationItem` from the domain layer
- Change `MonthEndStatusOverview.clarifications` from `List<MonthEndOverviewClarificationItem>` to `List<MonthEndClarification>` — the application layer passes raw aggregates through; enrichment is no longer its concern
- Move clarification enrichment entirely to the REST adapter: the inbound adapter resolves `UserId → UserRef` by calling `MonthEndUserSnapshotPort.findByIds()` directly, and evaluates capability flags via `actorId` from the security context
- Add `canEditText` and `canDelete` to the OpenAPI overview clarification entry alongside the existing `canResolve`
- Mutation endpoints (create, update, complete) return the same enriched clarification DTO shape as the overview entry — no round-trip re-fetch needed after mutations

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `monthend-clarifications`: Clarification display enrichment (UserRef resolution, capability flag evaluation) is removed from the application layer and is exclusively a REST adapter concern; `MonthEndStatusOverview` holds `List<MonthEndClarification>`; `MonthEndOverviewClarificationItem` is removed
- `monthend-rest-api`: Overview clarification entries gain `canEditText` and `canDelete` fields; mutation endpoints (create, update, complete) return an enriched clarification response with full UserRefs and all three capability flags; REST adapter resolves user display data via `MonthEndUserSnapshotPort`

## Impact

- `MonthEndOverviewClarificationItem` — deleted
- `MonthEndStatusOverview` — `clarifications` field type changes from `List<MonthEndOverviewClarificationItem>` to `List<MonthEndClarification>`
- `AssembleMonthEndStatusOverviewService` — clarification item-building logic removed; raw `List<MonthEndClarification>` passed directly into `MonthEndStatusOverview`
- OpenAPI spec — `MonthEndOverviewClarificationEntry` gains `canEditText` and `canDelete`; create/update/complete clarification responses use the same enriched shape
- REST adapter mapper — new clarification enrichment mapping method: `(MonthEndClarification, Map<UserId, UserRef>, UserId actorId) → DTO`; inbound adapters inject and call `MonthEndUserSnapshotPort` for UserRef resolution before mapping
- Tests — overview assembly tests updated to assert raw clarification pass-through; REST mapper tests added for enrichment logic; REST resource tests assert `canEditText` and `canDelete` in all clarification responses
