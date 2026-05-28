## Context

The `ZepProject` REST DTO already carries a `billingType` field (a `ZepBillingType` record with an `id`). The legacy domain mapped this to a `BillabilityPreset` enum (BILLABLE=1, BILLABLE_FIXED=2, NOT_BILLABLE=3, NOT_BILLABLE_FIXED=4) and exposed an `isBillable()` method. The hexagonal `Project` aggregate currently ignores this field entirely.

The `ZepProjectMapper` (MapStruct, already in use) maps `ZepProject` → `ZepProjectProfile`. Extending it to derive a `billable` boolean from `billingType.id()` is the natural place for the derivation logic.

## Goals / Non-Goals

**Goals:**
- Expose `billable: boolean` on `Project` and `ZepProjectProfile` so consumers (month-end generation) can filter projects without re-querying ZEP
- Keep the domain model simple — a boolean is sufficient; the nuance between BILLABLE and BILLABLE_FIXED is an infrastructure detail

**Non-Goals:**
- Exposing the raw `BillabilityPreset` enum or billing type ID in the hexagonal domain
- Any REST API changes
- Any month-end generation logic (that is the `monthend-checks` change)

## Decisions

### Derive `billable` as a boolean, not an enum

**Decision:** `ZepProjectProfile` and `Project` carry `billable: boolean`, not a `BillingType` enum.

**Rationale:** Month-end generation only needs to know whether a project is customer-facing. The distinction between BILLABLE and BILLABLE_FIXED has no behavioural consequence in the domain. Keeping a plain boolean avoids leaking ZEP infrastructure concepts into the domain model.

**Alternative considered:** Mirror the legacy `BillabilityPreset` enum into the hexagonal domain. Rejected because it adds complexity with no current value.

### Derive billability in `ZepProjectMapper`

**Decision:** The MapStruct `ZepProjectMapper` uses a `@Named` default method `isBillable(ZepBillingType)` to convert `billingType.id()` to a boolean (id 1 or 2 → true, otherwise false / null → false).

**Rationale:** This keeps the derivation logic co-located with the ZEP-to-domain translation, consistent with the existing `toLocalDate` helper in the same mapper.

### `billable` is a mutable field updated by `syncFromZep`

**Decision:** `billable` is treated the same as `name` and `endDate` — it can change between syncs and is updated by `Project.syncFromZep(ZepProjectProfile)`.

**Rationale:** A project's billing arrangement can change over time. Treating it as immutable would require deleting and recreating projects to reflect changes.

## Risks / Trade-offs

- **Null `billingType` from ZEP** → defaults to `billable = false`. Conservative and safe.
- **Schema migration**: adding a NOT NULL column with a default requires care for existing rows. The Liquibase migration should add the column as `DEFAULT false` so existing projects are non-billable until the next sync updates them.

## Migration Plan

1. Add Liquibase changelog: `ALTER TABLE project ADD COLUMN billable BOOLEAN NOT NULL DEFAULT false`
2. Deploy — existing projects default to `billable = false`
3. Next sync cycle populates correct values for all projects from ZEP
4. No rollback concern — column addition is safe; removing it would require another migration
