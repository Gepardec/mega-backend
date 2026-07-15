# Recognition REST API

## Purpose

Defines the HTTP endpoint exposed by the Recognition bounded context for submitting recognition entries, together with the authentication and authorization rules that restrict submission to authenticated internal employees holding the `EMPLOYEE` role.

## Requirements

### Requirement: Authenticated employees can submit a recognition entry
The system SHALL expose an endpoint that lets an authenticated user submit a recognition entry. The request body SHALL contain a `message` field (the free-text description) and a `category` field (praise/appreciation or courage), and MAY contain an `anonymous` boolean flag. The `anonymous` flag SHALL default to `false`. On success the system SHALL persist a new entry and respond with a success status.

#### Scenario: Employee submits a valid recognition entry
- **WHEN** an authenticated employee sends a submit request with a non-empty `message` and a valid `category`
- **THEN** a new recognition entry is stored with status "new"
- **THEN** the response indicates the submission succeeded

#### Scenario: Omitted anonymous flag defaults to non-anonymous submission
- **WHEN** an authenticated employee sends a valid submit request without an `anonymous` flag
- **THEN** the submission is treated as non-anonymous
- **THEN** the stored entry contains the authenticated employee's user ID as its submitter

#### Scenario: Anonymous submission does not persist submitter information
- **WHEN** an authenticated employee sends a valid submit request with `anonymous` set to `true`
- **THEN** the stored entry contains no information about the authenticated employee as its submitter

#### Scenario: Submission with a blank message is rejected
- **WHEN** an authenticated employee sends a submit request with a blank or missing `message`
- **THEN** no entry is stored
- **THEN** the response indicates a client error

#### Scenario: Submission with an invalid category is rejected
- **WHEN** an authenticated employee sends a submit request with a `category` that is neither praise/appreciation nor courage
- **THEN** no entry is stored
- **THEN** the response indicates a client error

### Requirement: Submitting a recognition entry requires an internal employee
The submit endpoint SHALL require the caller to be authenticated, to hold the `EMPLOYEE` role, and to be an internal employee. Unauthenticated callers, authenticated callers lacking the `EMPLOYEE` role, and external employees SHALL be rejected and SHALL NOT create an entry.

#### Scenario: Unauthenticated request is rejected
- **WHEN** an unauthenticated caller sends a submit request
- **THEN** the request is rejected as unauthorized
- **THEN** no entry is stored

#### Scenario: Caller without the employee role is rejected
- **WHEN** an authenticated caller that does not hold the `EMPLOYEE` role sends a submit request
- **THEN** the request is rejected as forbidden
- **THEN** no entry is stored

#### Scenario: External employee is rejected
- **WHEN** an authenticated employee that is external sends a submit request
- **THEN** the request is rejected as forbidden
- **THEN** no entry is stored
