## Context

See proposal.md — Why.

Three facts about the toolchain shape every decision below.

1. **The service always writes `null`.** The REST layer uses `quarkus-rest-jackson`, and Jackson's default inclusion is `ALWAYS`. `quarkus.jackson.serialization-inclusion` is an optional config property with no default and is not set in `application.yaml`, and there is no `@JsonInclude` or `ObjectMapperCustomizer` anywhere in `src/`. Serializing the generated DTOs with a default `ObjectMapper` confirms it: a `UserRefDto` with no ZEP username emits `{"id":…,"fullName":…,"zepUrl":null}`. Had the stack used JSON-B/Yasson instead, the default would have been the opposite.

2. **In OpenAPI, `required` and `nullable` are orthogonal.** `required` governs key presence, `nullable` governs the value. Only `required` + `nullable` together describe "always present, may be null"; `required` alone would assert the field is never null, which is a worse misstatement than the current one.

3. **`nullable` on a `$ref` sibling is silently discarded.** OAS 3.0 ignores siblings of `$ref`, so `subjectEmployee: {$ref: …, nullable: true}` bundled down to a plain `$ref` and generated clients saw no `null` at all. The nullability has to be expressed as `allOf: [{$ref: …}]` plus `nullable: true`.

## Goals / Non-Goals

**Goals:**

- The contract describes what the service actually puts on the wire, so generated clients need no hand-written correction layer.
- Nullability survives bundling for `$ref` properties as well as scalars.
- `User.roles` is a closed, generated vocabulary on both sides of the contract.

**Non-Goals:**

- No change to the wire format, endpoints, status codes, or field names. A client reading today's responses sees byte-identical payloads.
- No attempt to make request-body properties required (see Decisions).
- No change to domain models. `worktime-warnings` and `user-aggregate` keep their existing domain-level requirements; only the REST representation is restated.

## Decisions

### Response properties become `required` + `nullable`; request properties do not

The `jaxrs-spec` generator derives `@NotNull` from `required` alone and never consults `nullable`. This is a defect in its model template rather than a reading of the spec — `pojo.mustache` applies `beanValidation.mustache`, which emits `{{#required}}@NotNull{{/required}}`, while the generator's own `bodyParams.mustache` does guard the same annotation with `{{^isNullable}}`.

For **response** DTOs the stray `@NotNull` is inert: `returnResponse=true` means every generated resource method returns a raw `Response`, no return type is `@Valid`-annotated, and there is no `@Valid` anywhere in `src/main/java`. Outbound DTOs are never bean-validated.

For **request** DTOs it is not inert: generated interfaces annotate body parameters `@Valid @NotNull`, so validation cascades into the bean and the property-level `@NotNull` is enforced. Marking `CreateClarificationRequest.subjectEmployeeId` or `ResolveClarificationRequest.resolutionNote` as required would reject a legitimate `null` with `400`. `ResolveClarificationRequest.resolutionNote` is the sharper case, because the existing frontend already sends an explicit `null` there.

*Alternative considered:* setting `openApiNullable=true` to get `JsonNullable<T>` wrappers that model the distinction properly. Rejected — `JsonNullable` appears nowhere in the `jaxrs-spec` templates; regenerating with it enabled produces byte-identical sources apart from one unused import. It is a no-op for this generator.

### `Role` is declared in `schemas/shared.yaml`, including `SYSTEM`

`Role` mirrors the shared domain enum, so it belongs beside the other cross-cutting schemas rather than in `user.yaml`, even though `User` is currently its only referent.

`SYSTEM` is included because the contract's job is to describe what the field can contain, and the generated enum is the target of a total mapping from the domain enum. Omitting it would make the mapping partial: MapStruct generates an exhaustive `switch` with `default: throw new IllegalArgumentException(...)`, so a missing constant fails the **build**, and `RoleDto.fromValue("SYSTEM")` would throw at runtime for any system actor that reached serialization.

*Consequence for clients:* a generated role vocabulary now includes a value most consumers have no use for. That is a consumer-side filtering concern, not a reason to understate the contract.

### The CSV template response declares `format: binary`

The generated Java signature is unaffected — the method already returns a raw `Response`, and only a documentation annotation changes to `@Schema(implementation = File.class)`. The resource keeps returning the same string body with the same `text/csv` media type. The benefit is downstream: clients receive a binary body instead of a decoded string, which is what callers of a file download need.

## Risks / Trade-offs

- **`@NotNull` on response DTO getters is misleading to read** → It is unenforced for the reasons above, and the alternative (leaving the contract wrong) costs every consumer a correction layer. Revisit if response validation is ever switched on, or if `jaxrs-spec` fixes its model template.
- **Request and response properties now follow different conventions** → Deliberate and documented here; the asymmetry is forced by the generator, not chosen. A reader comparing `MonthEndOverviewClarificationEntry.resolutionNote` (required) with `ResolveClarificationRequest.resolutionNote` (optional) should find this section.
- **A future backend-only role addition breaks the contract silently for clients** → The MapStruct `switch` makes the *backend* fail to build until the contract enum is extended, so the contract cannot drift behind the domain unnoticed. Clients generated against an older contract would still receive an unmodelled string; consumers that narrow roles defensively are unaffected.
- **Consumers that treated a missing key as meaningful** → None exist: the service already sent `null` rather than omitting keys, so no payload changes. This corrects the contract, not the behaviour.

## Migration Plan

No deployment coordination is required — the wire format does not change, so old and new clients both work against either version of the service. Downstream client regeneration is independent and can follow at any time.
