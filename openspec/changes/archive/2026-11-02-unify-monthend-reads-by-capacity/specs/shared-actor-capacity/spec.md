## Purpose

Defines the capacity a caller acts in when reading data: the closed set of capacities, the role each requires, how capacity-scoped read endpoints accept it, and the rule that capacity shapes reads but never authorizes or restricts commands.

## ADDED Requirements

### Requirement: Callers read capacity-scoped data in one of two capacities
The system SHALL define exactly two capacities: `EMPLOYEE` and `CONTROLLING`. The `EMPLOYEE` capacity SHALL require the employee role, and the `CONTROLLING` capacity SHALL require the project-lead role. A caller holding both roles SHALL be able to read in either capacity, independently and in any order.

#### Scenario: Caller holding both roles reads in each capacity
- **WHEN** an authenticated caller holding both the employee and the project-lead role requests a capacity-scoped read once with `capacity=EMPLOYEE` and once with `capacity=CONTROLLING`
- **THEN** the API accepts both requests
- **THEN** each response contains the data of the requested capacity

#### Scenario: Caller without the project-lead role cannot read in the controlling capacity
- **WHEN** an authenticated caller without the project-lead role requests a capacity-scoped read with `capacity=CONTROLLING`
- **THEN** the API rejects the request with `403 Forbidden`

#### Scenario: Caller without the employee role cannot read in the employee capacity
- **WHEN** an authenticated caller without the employee role requests a capacity-scoped read with `capacity=EMPLOYEE`
- **THEN** the API rejects the request with `403 Forbidden`

### Requirement: Capacity-scoped read endpoints require an explicit capacity
Every capacity-scoped read endpoint SHALL accept the capacity as a required `capacity` query parameter whose values are `EMPLOYEE` and `CONTROLLING`. The system SHALL NOT infer a capacity from the caller's roles or apply a default.

#### Scenario: Request without a capacity is rejected
- **WHEN** an authenticated caller requests a capacity-scoped read endpoint without the `capacity` query parameter
- **THEN** the API rejects the request with `400 Bad Request`

#### Scenario: Request with an unknown capacity is rejected
- **WHEN** an authenticated caller requests a capacity-scoped read endpoint with a `capacity` value other than `EMPLOYEE` or `CONTROLLING`
- **THEN** the API rejects the request with a client error response
- **THEN** no data is returned

### Requirement: Capacity shapes reads but never authorizes commands
Capacity SHALL only determine which records a read returns and which actions the read offers through its capability flags. Commands SHALL NOT accept a capacity, and SHALL be authorized only by the acting caller and the affected record. An action that a read does not offer in one capacity SHALL still be accepted by its command endpoint when the caller is eligible for it.

#### Scenario: Action not offered in the current capacity is still accepted by the command
- **WHEN** a capacity-scoped read returns a record with a capability flag set to `false` only because the action belongs to the other capacity
- **AND** the caller is eligible to perform that action on the record
- **THEN** the corresponding command endpoint accepts the action from that caller
