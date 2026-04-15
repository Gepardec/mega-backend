## ADDED Requirements

### Requirement: Layer dependency direction is enforced by ArchUnit
The hexagon package SHALL enforce the following dependency directions via ArchUnit tests. The domain layer is the innermost ring and MAY NOT depend on any outer layer. The application layer MAY NOT depend on the adapter layer. Adapters on the inbound and outbound sides MUST NOT depend on each other.

#### Scenario: Domain does not depend on the application layer
- **WHEN** any class resides in `..hexagon..domain..`
- **THEN** it SHALL NOT import or reference any class residing in `..hexagon..application..`

#### Scenario: Domain does not depend on the adapter layer
- **WHEN** any class resides in `..hexagon..domain..`
- **THEN** it SHALL NOT import or reference any class residing in `..hexagon..adapter..`

#### Scenario: Application does not depend on the adapter layer
- **WHEN** any class resides in `..hexagon..application..`
- **THEN** it SHALL NOT import or reference any class residing in `..hexagon..adapter..`

#### Scenario: Inbound adapter does not depend on outbound adapter
- **WHEN** any class resides in `..hexagon..adapter..inbound..`
- **THEN** it SHALL NOT import or reference any class residing in `..hexagon..adapter..outbound..`

#### Scenario: Outbound adapter does not depend on inbound adapter
- **WHEN** any class resides in `..hexagon..adapter..outbound..`
- **THEN** it SHALL NOT import or reference any class residing in `..hexagon..adapter..inbound..`

### Requirement: Domain models are immutable records or enums
All types residing in `..hexagon..domain..model..` that are not enums SHALL be Java records. This enforces immutability and prevents regular mutable classes from being introduced as domain models.

#### Scenario: Domain model is a record
- **WHEN** a non-enum type resides in `..hexagon..domain..model..`
- **THEN** it SHALL be a Java record (extend `java.lang.Record`)

#### Scenario: Domain model enum is permitted
- **WHEN** an enum type resides in `..hexagon..domain..model..`
- **THEN** it SHALL be permitted without the record constraint

### Requirement: Domain models do not carry JPA annotations
Types residing in `..hexagon..domain..model..` SHALL NOT be annotated with any JPA persistence annotation (`@Entity`, `@Table`, `@Column`, `@MappedSuperclass`, `@Embeddable`). JPA annotations are adapter concerns and MUST NOT leak into the domain layer.

#### Scenario: Domain model has no @Entity annotation
- **WHEN** a type resides in `..hexagon..domain..model..`
- **THEN** it SHALL NOT be annotated with `@Entity`

#### Scenario: Domain model has no @Table annotation
- **WHEN** a type resides in `..hexagon..domain..model..`
- **THEN** it SHALL NOT be annotated with `@Table`

#### Scenario: Domain model has no @Column annotation
- **WHEN** a type resides in `..hexagon..domain..model..`
- **THEN** it SHALL NOT be annotated with `@Column`

### Requirement: Inbound ports are interfaces ending with UseCase
All types in `..hexagon..application..port..inbound..` that are interfaces SHALL have their simple name ending with `UseCase`. Non-interface types (result records co-located with use case contracts) SHALL be Java records.

#### Scenario: UseCase interface follows naming convention
- **WHEN** an interface resides in `..hexagon..application..port..inbound..`
- **THEN** its simple name SHALL end with `UseCase`

#### Scenario: Non-interface in inbound port package is a record
- **WHEN** a non-interface type resides in `..hexagon..application..port..inbound..`
- **THEN** it SHALL be a Java record

### Requirement: Outbound ports are interfaces
All types residing in `..hexagon..domain..port..outbound..` SHALL be interfaces. Concrete implementations are adapter concerns and MUST NOT reside in the domain port package.

#### Scenario: Outbound port is an interface
- **WHEN** any type resides in `..hexagon..domain..port..outbound..`
- **THEN** it SHALL be an interface

### Requirement: Application services follow naming and CDI conventions
Classes in `..hexagon..application..` that implement a `*UseCase` interface SHALL have their simple name ending with `Service` and SHALL be annotated with `@ApplicationScoped`.

#### Scenario: UseCase implementation ends with Service
- **WHEN** a class in `..hexagon..application..` implements an interface whose name ends with `UseCase`
- **THEN** the class simple name SHALL end with `Service`

#### Scenario: UseCase implementation is ApplicationScoped
- **WHEN** a class in `..hexagon..application..` implements an interface whose name ends with `UseCase`
- **THEN** the class SHALL be annotated with `@ApplicationScoped`

### Requirement: JPA entities are confined to the outbound adapter layer
Classes annotated with `@Entity` in `..hexagon..` SHALL reside exclusively in `..hexagon..adapter..outbound..` and SHALL have their simple name ending with `Entity`.

#### Scenario: @Entity class is in adapter/outbound
- **WHEN** a class in `..hexagon..` is annotated with `@Entity`
- **THEN** it SHALL reside in `..hexagon..adapter..outbound..`

#### Scenario: @Entity class ends with Entity
- **WHEN** a class in `..hexagon..adapter..outbound..` is annotated with `@Entity`
- **THEN** its simple name SHALL end with `Entity`

### Requirement: Panache repositories are confined to the outbound adapter layer
Classes that extend `PanacheRepository` in `..hexagon..` SHALL reside exclusively in `..hexagon..adapter..outbound..`. Panache is an infrastructure concern and MUST NOT appear in the domain or application layers.

#### Scenario: PanacheRepository subclass is in adapter/outbound
- **WHEN** a class in `..hexagon..` extends `PanacheRepository`
- **THEN** it SHALL reside in `..hexagon..adapter..outbound..`
