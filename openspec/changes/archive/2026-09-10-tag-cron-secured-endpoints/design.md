## Context

See proposal.md — Why.

The relevant constraint is that tags in the canonical contract are not just documentation. The JAX-RS server generator runs with `useTags=true`, so one tag produces one generated API interface, and that interface carries a class-level `@Path` derived from the operations it holds. Consumers generate the same way: the frontend runs orval in `tags-split` mode, where a tag becomes a service class. A tag is therefore the natural unit of "things a consumer can accept or reject wholesale" on both sides of the contract.

Before this change all monthend operations shared the `MonthEnd` tag, so `POST /monthend/{month}/generate` — reachable only with a `mega-cron` client-credentials token — was indistinguishable from the actor-facing operations to any tag-based tooling.

An earlier change (`merge-monthend-rest-resources`, archived 2026-04-20) deliberately collapsed `MonthEndEmployee`, `MonthEndProjectLead`, `MonthEndShared`, and `MonthEndOps` into the single `MonthEnd` tag. That change also recorded the fallback this design takes up: "extract ops back to its own class if needed (no domain changes required)."

## Goals / Non-Goals

**Goals:**

- Make "is this operation callable from a browser session?" answerable from the contract alone, by tag, without a path list.
- Keep the guarantee open-ended: a future machine-to-machine endpoint should be excluded by consumers the moment it carries the tag, with no consumer-side change.
- Leave routing, payloads, and security enforcement untouched.

**Non-Goals:**

- Re-splitting the actor-facing monthend operations. The employee/project-lead partition stays inside the single `MonthEnd` tag; consumers that need that partition do it themselves (the frontend does it with facades).
- Moving the operational endpoint to a separate contract document, a separate path prefix, or a separate service.
- Changing which roles or tenants may call the endpoint.

## Decisions

### Tag the endpoint rather than filter it downstream

The alternative in place until now was for each consumer to strip the endpoint by path. That works, but it puts the knowledge in the wrong repository: the frontend had to know which backend endpoints are operational, and its list went stale silently — a newly added cron endpoint would be generated into the browser client with nothing failing. Encoding the distinction in the contract makes the backend the single owner of that fact, and makes every consumer's exclusion rule a constant.

A tag named for the security posture (`Cron`) rather than for a feature area is deliberate: it is what the exclusion rule keys on, and it generalises to any future client-credentials endpoint regardless of which bounded context it belongs to.

### Accept the resource split that `useTags=true` forces

Because the generator derives a class-level `@Path` per tag, `CronApi` gets `@Path("/monthend/{month}/generate")` while `MonthEndApi` keeps `@Path("/monthend")`. A single class cannot inherit both, so the operation moves into its own adapter class alongside the existing one, carrying the same tenant and role constraints it had as a method annotation.

Alternatives considered:

- **Turn off `useTags`** — would rename and restructure every generated interface across all four tags for the sake of one operation.
- **Give the operation both tags** — the generator picks one for the interface anyway, and a consumer excluding by tag would still see it appear under the tag it kept. It would also make "no actor-facing endpoint carries the `Cron` tag" untrue in the other direction.
- **Keep one adapter class and hand-write the operational endpoint** — reintroduces the handwritten endpoint signature that `monthend-rest-api` explicitly requires against.

The split is a smaller cost than it looks: the operational endpoint shares no state with the actor-facing ones. It does not use the authenticated actor context, and needs only the generation use case plus the month parser and result mapper.

## Risks / Trade-offs

- **Tag proliferation** → A security-posture tag cutting across bounded contexts sits oddly beside the feature-area tags. Accepted: the alternative is per-context operational tags, which puts the maintenance burden back on consumers, who would each have to track a growing list.
- **Reverses part of an archived decision** → `merge-monthend-rest-resources` merged `MonthEndOps` into `MonthEnd` to get one adapter class. That merge is now partly undone. Accepted, and anticipated: that design listed this extraction as its stated fallback, and only the operational endpoint moves — the three actor-facing tags stay merged.
- **Silent regression if the tag is removed** → Retagging the endpoint back onto `MonthEnd` would restore a browser-callable cron client with no build failure on either side. Mitigated by the spec requirement; a contract check asserting no actor-facing endpoint carries the `Cron` tag is a possible follow-up.
- **Consumers keyed on the old grouping** → Any consumer generating code grouped by tag sees the operation move to a different generated class. Only `mega-frontend-v2` consumes this contract, and it excludes the operation entirely.

## Migration Plan

The endpoint's path and authorization are unchanged, so backend and frontend can be deployed independently and in either order.

1. Retag the operation in the contract and add the tag definition.
2. Regenerate server interfaces; move the operation into its own adapter class with the same tenant and role constraints, and move its tests alongside it.
3. In `mega-frontend-v2`, refresh the vendored contract snapshot and swap the path-based exclusion for a tag filter. Generated output is expected to be unchanged.
