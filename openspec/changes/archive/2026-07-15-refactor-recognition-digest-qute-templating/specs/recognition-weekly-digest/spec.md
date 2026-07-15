## MODIFIED Requirements

### Requirement: The digest contains all entries not yet included in a previous digest
Each weekly digest SHALL contain every recognition entry whose status is "new" at the time the digest is assembled, and SHALL NOT contain entries already included in a previous digest. The digest content SHALL present each entry's message, its category (praise/appreciation or courage), and its submitter attribution. When an entry has a recorded `submittedBy` value, the attribution SHALL show that user's display name; when `submittedBy` is absent, it SHALL show the literal `Anonym`. Entry messages and submitter names SHALL be presented as text: any characters that are significant to the digest's markup SHALL be shown to the recipient as literal text and SHALL NOT be interpreted as markup.

#### Scenario: New entries appear in the digest
- **WHEN** the digest is assembled and there are entries with status "new"
- **THEN** every such entry is included in the digest, showing its message, category, and submitter attribution

#### Scenario: Recorded submitter appears by name
- **WHEN** a new recognition entry has a `submittedBy` value
- **THEN** its digest entry shows the corresponding submitter display name

#### Scenario: Anonymous submitter is labeled explicitly
- **WHEN** a new recognition entry has no `submittedBy` value
- **THEN** its digest entry shows `Anonym` as the submitter attribution

#### Scenario: Previously included entries do not reappear
- **WHEN** the digest is assembled
- **THEN** entries already marked "included in digest" are not part of the digest

#### Scenario: Entry content with markup-significant characters is shown as text
- **WHEN** a "new" entry's message or submitter name contains characters that are significant to the digest's markup
- **THEN** those characters appear in the digest as literal text to the recipient
- **AND** they are not interpreted as markup
