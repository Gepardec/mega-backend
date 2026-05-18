---
name: openspec-check-spec-language
description: Proactively scan spec files in a change for programming-language-specific content that belongs in design.md or tasks.md instead.
user-invocable: false
---

Run this check proactively after spec files are created or modified.

## What to flag

- Class or type names (PascalCase identifiers, e.g. `ZepEmployeeAdapter`)
- Method or field references (camelCase followed by parentheses, or dot-notation chains)
- Annotation syntax (`@Something`)
- Framework or library names (Mutiny, MapStruct, JAX-RS, Hibernate, Panache, Quarkus-specific terms)
- Code snippets (inline expressions, lambda syntax, chained calls)
- Cron expressions
- SQL / DDL fragments (column definitions, data types)
- Logging level names used as requirements (`ERROR`, `INFO`, `WARN`)

## What is acceptable in specs

- REST paths and HTTP status codes
- JSON field names that form part of the API contract
- Domain status names (`DONE`, `OPEN`, etc.)
- Concrete date or value examples in scenarios
- Role names (`OFFICE_MANAGEMENT`, etc.)

## Output

For each flagged item, quote the offending line and suggest a behaviour-focused rewrite. Group findings by file.

If nothing is flagged, say nothing.
