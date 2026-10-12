## Why

The monthend status overview displays employees and projects, but there is no way to navigate from an overview entry to the corresponding record in ZEP. Users must manually look up the employee or project in ZEP, which is slow and error-prone. Adding a direct deep link per entry removes that friction.

## What Changes

- `ProjectRef` and `UserRef` API responses gain a `zepUrl` field — a pre-assembled, ready-to-use href pointing to the corresponding record in the ZEP web interface
- `ZepConfig` gains two URL-building methods that combine the existing `mega.zep.origin` with hardcoded sub-paths specific to ZEP's employee and project pages
- `MonthEndRestMapper` is updated to populate `zepUrl` on both reference types at mapping time

## Capabilities

### New Capabilities

_(none — this change only extends existing shared reference types and an existing mapper)_

### Modified Capabilities

- `shared-user-project-refs`: `ProjectRef` and `UserRef` response shapes gain a new nullable `zepUrl` field

## Impact

- **OpenAPI schema** (`shared.yaml`): `ProjectRef` and `UserRef` schemas gain `zepUrl: string` (nullable)
- **Generated DTOs**: `ProjectRefDto` and `UserRefDto` regenerated with the new field
- **`MonthEndRestMapper`**: converted from interface to abstract class to allow CDI injection of `ZepConfig`; `toDto(ProjectRef)` and `toDto(UserRef)` implemented manually
- **`ZepConfig`** (legacy package): two new URL-builder methods added
- **All consumers of `ProjectRefDto` / `UserRefDto`** (monthend, worktime REST adapters) receive the field automatically — no additional mapper changes required
- No breaking changes; `zepUrl` is a new nullable field on existing response types
