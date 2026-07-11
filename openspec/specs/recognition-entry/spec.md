# Recognition Entry

## Purpose

Defines the recognition entry (Briefkasten) domain concept: a submitted, immutable record capturing a free-text message about a colleague's deed and its category (praise/appreciation or courage), an optional submitter identity, and its two-state lifecycle from "new" to "included in digest".

## Requirements

### Requirement: A recognition entry captures a free-text message and a category
A recognition entry SHALL hold a non-empty free-text message describing a colleague's deed, a category, a submission timestamp, and a lifecycle status. The message SHALL be stored verbatim as submitted; the system SHALL NOT parse, split, or require any structured reference to the praised colleague.

#### Scenario: Entry is created with a message and a category
- **WHEN** a recognition entry is submitted with a non-empty message and a valid category
- **THEN** the entry is stored with that message, that category, a submission timestamp, and the status "new"

#### Scenario: Empty message is rejected
- **WHEN** a recognition entry is submitted with a blank or missing message
- **THEN** the entry is not stored and the submission is rejected

### Requirement: A recognition entry retains the submitter only when not anonymous
For a non-anonymous submission, the recognition entry SHALL store the authenticated submitter's user ID. For an anonymous submission, the recognition entry SHALL NOT persist any information about the submitter.

#### Scenario: Non-anonymous entry stores the submitter user ID
- **WHEN** an authenticated employee submits a recognition entry without choosing anonymity
- **THEN** the stored entry contains that employee's user ID as its submitter

#### Scenario: Anonymous entry does not retain submitter information
- **WHEN** an authenticated employee submits a recognition entry anonymously
- **THEN** the stored entry contains no information about that employee as its submitter

### Requirement: A recognition entry is classified as praise or courage
Every recognition entry SHALL carry exactly one category with one of two values: praise/appreciation (Lob/Wertschätzung) or courage (Mut). The category SHALL be provided at submission time and SHALL NOT change afterwards.

#### Scenario: Entry classified as praise
- **WHEN** an entry is submitted with the praise/appreciation category
- **THEN** the stored entry's category is praise/appreciation

#### Scenario: Entry classified as courage
- **WHEN** an entry is submitted with the courage category
- **THEN** the stored entry's category is courage

#### Scenario: Unknown category is rejected
- **WHEN** an entry is submitted with a category that is neither praise/appreciation nor courage
- **THEN** the entry is not stored and the submission is rejected

### Requirement: A recognition entry moves through a two-state lifecycle
A recognition entry SHALL start in the status "new" when created and SHALL transition to "included in digest" once it has been sent in a weekly digest. The transition SHALL be one-directional; an entry that is "included in digest" SHALL NOT return to "new".

#### Scenario: New entry becomes included after being sent in a digest
- **WHEN** a "new" entry is sent as part of a weekly digest
- **THEN** the entry's status becomes "included in digest"

#### Scenario: Already included entry is not sent again
- **WHEN** a weekly digest is assembled
- **THEN** entries whose status is "included in digest" are not part of the digest

### Requirement: Recognition entries are immutable and permanent
Once created, a recognition entry's message and category SHALL NOT be editable, and the entry SHALL NOT be deletable or archivable. The only permitted change to a stored entry is the one-directional lifecycle transition from "new" to "included in digest".

#### Scenario: Entry content cannot be changed
- **WHEN** an attempt is made to modify a stored entry's message or category
- **THEN** the system provides no operation to do so and the stored content is unchanged

#### Scenario: Entry cannot be removed
- **WHEN** an entry has been stored
- **THEN** the system provides no operation to delete or archive it
