## Why

The monthend REST layer is split across four resource classes (`MonthEndEmployeeResource`, `MonthEndProjectLeadResource`, `MonthEndSharedResource`, `MonthEndOpsResource`) and three generated OpenAPI interfaces, requiring clients to know whether they are calling the "employee" or "project-lead" variant of each operation — even though every user holds the employee role and project leads hold both. This creates unnecessary client friction, duplicated helper methods across resource classes, and scattered endpoint logic that should be cohesive.

## What Changes

- Replace the four resource classes with a single `MonthEndResource` class backed by one generated `MonthEndApi` interface
- Drop the `/employee/` and `/project-lead/` URL prefixes; the server dispatches to the appropriate use case based on the caller's roles
- Encode month in the path for `status-overview` and `generate`: `/monthend/{month}/status-overview` and `POST /monthend/{month}/generate`
- Merge `CreateEmployeeClarificationRequest` and `CreateProjectLeadClarificationRequest` into a single `CreateClarificationRequest` with optional `subjectEmployeeId`
- Consolidate the three OpenAPI tags (`MonthEndEmployee`, `MonthEndProjectLead`, `MonthEndShared`) into a single `MonthEnd` tag; `MonthEndOps` merges into the same tag
- **BREAKING**: all existing monthend endpoint paths change

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `monthend-rest-api`: URL structure changes (month-in-path for two endpoints, role prefixes removed), single resource class, merged clarification create DTO, consolidated OpenAPI tag and generated interface

## Impact

- `src/main/resources/openapi/paths/monthend.yaml` — all path keys and tags updated
- `src/main/resources/openapi/schemas/monthend.yaml` — `CreateEmployeeClarificationRequest` and `CreateProjectLeadClarificationRequest` replaced by `CreateClarificationRequest`
- `MonthEndEmployeeResource`, `MonthEndProjectLeadResource`, `MonthEndSharedResource`, `MonthEndOpsResource` — deleted, replaced by `MonthEndResource`
- Generated interfaces `MonthEndEmployeeApi`, `MonthEndProjectLeadApi`, `MonthEndSharedApi`, `MonthEndOpsApi` — replaced by `MonthEndApi`
- Frontend and any other API clients must update all monthend endpoint URLs
