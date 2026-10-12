## MODIFIED Requirements

### Requirement: Project is decoupled from Quarkus and JPA infrastructure
The `Project` aggregate and its supporting value objects (`ZepProjectProfile`) SHALL NOT import or depend on any Quarkus, CDI, or JPA annotations. `ProjectId` is now a shared kernel type and is imported from `com.gepardec.mega.hexagon.shared.domain.model`.

#### Scenario: Project class has no framework imports
- **WHEN** `Project.java` is compiled
- **THEN** it imports only from `com.gepardec.mega.hexagon.project`, `com.gepardec.mega.hexagon.shared.domain.model`, `java.*`, and standard libraries
