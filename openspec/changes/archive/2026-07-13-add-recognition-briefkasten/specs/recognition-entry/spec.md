## ADDED Requirements

### Requirement: A recognition entry captures a free-text message and a category
A recognition entry SHALL hold a non-empty free-text message describing a colleague's deed, a category, a submission timestamp, and a lifecycle status. The message SHALL be stored verbatim as submitted; the system SHALL NOT parse, split, or require any structured reference to the praised colleague or the submitter.

#### Scenario: Entry is created with a message and a category
- **WHEN** a recognition entry is submitted with a non-empty message and a valid category
- **THEN** the entry is stored with that message, that category, a submission timestamp, and the status "new"

#### Scenario: Empty message is rejected
- **WHEN** a recognition entry is submitted with a blank or missing message
- **THEN** the entry is not stored and the submission is rejected

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
