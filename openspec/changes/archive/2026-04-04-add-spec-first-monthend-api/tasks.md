## 1. OpenAPI Contract And Build Setup

- [x] 1.1 Create the canonical `src/main/openapi/monthend.openapi.yaml` file with employee, project-lead, shared, and ops tags, endpoint definitions, request schemas, response schemas, and security requirements.
- [x] 1.2 Add and configure `openapi-generator-maven-plugin` in [pom.xml](/Users/olivertod/dev/repos/mega-backend/pom.xml) to generate Jakarta JAX-RS interfaces and models into `target/generated-sources/openapi`.
- [x] 1.3 Verify the generated monthend API interfaces and models are available on the compile classpath and fit the intended package layout for handwritten REST adapters.

## 2. Boundary Refactor And HTTP Mapping

- [x] 2.1 Introduce a monthend REST-side actor resolver that reads the authenticated caller and resolves the hexagon `UserId` before invoking use cases.
- [x] 2.2 Refactor `PrematureMonthEndPreparationUseCase` and `PrematureMonthEndPreparationService` to accept `actorId` from the driver boundary and remove the current application-level auth lookup dependency.
- [x] 2.3 Add MapStruct mappers and transport conversion helpers for generated monthend request and response models, including `yyyy-MM` month parsing and UUID-to-domain ID conversion.

## 3. REST Adapter Implementation

- [x] 3.1 Implement the employee monthend REST adapter for worklist retrieval, self-service preparation, and employee clarification creation.
- [x] 3.2 Implement the project-lead monthend REST adapter for lead worklist retrieval and lead clarification creation.
- [x] 3.3 Implement the shared monthend REST adapter for status overview, task completion, clarification text updates, and clarification resolution.
- [x] 3.4 Implement the ops monthend REST adapter for authenticated internal generation requests.
- [x] 3.5 Apply resource-level security annotations for employee, project-lead, shared, and ops endpoint groups in line with the approved role policy.

## 4. Testing

- [x] 4.1 Add unit tests for the REST-side actor resolver and HTTP mapping helpers.
- [x] 4.2 Add `@QuarkusTest` REST integration tests for employee and project-lead monthend endpoints, including role-based access checks.
- [x] 4.3 Add `@QuarkusTest` REST integration tests for shared monthend actions and the internal ops generation endpoint, including forbidden-path scenarios.
- [x] 4.4 Update existing monthend application and integration tests impacted by the self-service preparation signature change.

## 5. Verification

- [ ] 5.1 Run the relevant monthend and REST test suites and fix any contract, mapping, or security regressions.
- [x] 5.2 Verify a standard Maven build regenerates the monthend OpenAPI interfaces and models cleanly without manual intervention.
