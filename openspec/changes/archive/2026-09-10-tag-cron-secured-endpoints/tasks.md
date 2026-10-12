## 1. Contract

- [x] 1.1 Add a `Cron` tag definition to the canonical OpenAPI document, describing it as the grouping for machine-to-machine endpoints secured by the client-credentials scheme; verify the tag appears in the bundled contract produced by the build
- [x] 1.2 Retag `POST /monthend/{month}/generate` from `MonthEnd` to `Cron`, leaving its path, parameters, responses, and security requirement untouched; verify the bundled contract shows the operation under `Cron` with an unchanged operation body
- [x] 1.3 Drop "operational callers" from the `MonthEnd` tag description so it describes only the actor-facing endpoints; verify no remaining operation carries both `MonthEnd` and the client-credentials security requirement

## 2. REST adapter

- [x] 2.1 Regenerate the server interfaces and confirm a separate operational API interface is produced holding only the generation operation; verify the generated sources contain it and that it is no longer a member of the month-end interface
- [x] 2.2 Move the generation endpoint into its own request-scoped adapter class implementing the new interface, carrying the same tenant and role constraints it had as method annotations, and depending only on the generation use case plus the month parser and result mapper; verify the project compiles
- [x] 2.3 Remove the generation endpoint and its now-unused dependency from the existing month-end adapter; verify the project compiles with no unused imports left behind

## 3. Tests

- [x] 3.1 Move the two generation endpoint tests — successful generation for a caller holding the cron role, and rejection of a caller without it — into a test class covering the new adapter; verify both pass
- [x] 3.2 Remove the moved tests and their now-unused mock from the existing month-end adapter test; verify that class still passes
- [x] 3.3 Run the full test suite and verify no regression in routing or authorization for any month-end endpoint
