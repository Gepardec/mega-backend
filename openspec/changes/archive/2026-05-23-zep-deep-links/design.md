## Context

`ProjectRef` and `UserRef` are shared-kernel reference types used throughout the hexagonal module's REST responses. Both carry ZEP-specific identifiers in the domain model (`ProjectRef.zepId: int`, `UserRef.zepUsername: ZepUsername`) that are not currently exposed in the generated DTOs. The ZEP web interface uses PHP-based URLs with query parameters; the base origin is already stored in `mega.zep.origin` and exposed to the frontend via the legacy `GET /config` endpoint. `MonthEndRestMapper` is currently a MapStruct interface that auto-generates `toDto(ProjectRef)` and `toDto(UserRef)` from matching field names.

## Goals / Non-Goals

**Goals:**
- Expose a pre-assembled `zepUrl` on `ProjectRefDto` and `UserRefDto` so the frontend can link directly to ZEP records without knowing ZEP's URL structure
- Keep ZEP URL construction fully server-side and centralized in `ZepConfig`

**Non-Goals:**
- Exposing raw `zepId` or `zepUsername` in the DTO schemas — the assembled URL is sufficient
- Adding a new config endpoint or modifying the legacy `GET /config` response
- Changes to worktime or other REST adapters — they receive the field automatically via schema regeneration

## Decisions

### Backend assembles the full ZEP URL (not the frontend)

The ZEP deep-link URLs contain multi-parameter PHP query strings (e.g., `menu=MitarbeiterVerwaltungMgr&modelContentMenu=true&mgr=MitarbeiterProjektzeitMgr&contentModelId=`). Assembling these on the frontend would require the frontend to know ZEP's URL structure. Backend assembly keeps that knowledge in one place.

Alternatives considered:
- **Expose raw identifiers + config-provided base URLs**: The frontend already receives `zepOrigin` from `GET /config`. Adding base URL templates there and `zepId`/`zepUsername` to the DTOs would work but spreads URL knowledge across frontend and backend. Rejected because the PHP query parameter format is not suited for simple string concatenation on the frontend.

### Sub-path format hardcoded in `ZepConfig`, not in `application.yaml`

The ZEP URL format is a stable third-party implementation detail that does not vary between environments — only `origin` differs. Adding config properties for the sub-paths would create configurable values that will never actually be changed. The methods `buildProjectUrl` and `buildEmployeeUrl` in `ZepConfig` hardcode the path while combining it with the already-configurable `origin`.

### `MonthEndRestMapper` converted from interface to abstract class

MapStruct cannot inject CDI beans into interfaces. Converting to an abstract class allows `@Inject ZepConfig zepConfig` and manual implementation of `toDto(ProjectRef)` and `toDto(UserRef)` while letting MapStruct continue to generate all other mapping methods. This is the standard MapStruct pattern for mappers that need injected dependencies.

Alternatives considered:
- **Pass `ZepConfig` as `@Context`**: Would require every call site to pass it, and MapStruct would need custom handling per call. Adds noise without benefit.
- **Pre-compute URL maps in the resource and pass as context**: Works but moves URL construction into the resource, which is the wrong layer.

### Direct import of `ZepConfig` from the legacy package

`ZepConfig` lives in `com.gepardec.mega.application.configuration`. `HexagonalArchitectureTest` imports only `com.gepardec.mega.hexagon` classes, so cross-package imports from the adapter layer to the legacy config are not checked or restricted. This is accepted as a pragmatic choice; no hexagonal wrapper is needed for a pure configuration value object.

## Risks / Trade-offs

- **ZEP URL format change** → The hardcoded sub-paths in `ZepConfig` would need updating. Mitigation: the format is stable (PHP legacy system) and the change is a one-line fix in a single method.
- **Null `zepUsername`** → `UserRef.zepUsername` is not null-checked in its constructor. `buildEmployeeUrl` must guard against null and return null for `zepUrl` in that case. Mitigation: mapper implements null check before calling `buildEmployeeUrl`.
- **All consumers of `ProjectRefDto`/`UserRefDto` receive the new field** → This is intentional; no consumer is harmed by an additional nullable field. The worktime REST adapter gets `zepUrl` for free with no code changes.
