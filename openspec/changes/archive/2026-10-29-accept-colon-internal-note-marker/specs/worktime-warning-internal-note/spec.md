## RENAMED Requirements

- FROM: `### Requirement: A note counts as internally marked when it starts with "Intern" and a dash`
- TO: `### Requirement: A note counts as internally marked when it starts with "Intern" and a delimiter`

## MODIFIED Requirements

### Requirement: A note counts as internally marked when it starts with "Intern" and a delimiter
A note SHALL count as internally marked if, after optional leading spaces, it starts with the word "Intern", regardless of upper or lower case, followed by optional spaces and a delimiter. Accepted delimiters SHALL be a hyphen (`-`), an en dash (`–`) or a colon (`:`). An empty or missing note SHALL NOT count as internally marked, nor SHALL a note that contains the marker only elsewhere.

#### Scenario: A note following the convention is marked
- **WHEN** a booking carries the note "Intern - Jenkins aufräumen"
- **THEN** the note counts as internally marked

#### Scenario: Deviating spellings are marked
- **WHEN** a booking carries one of the notes "intern - JFX", "Intern -JFX", "Intern– JFX" or "  Intern - JFX"
- **THEN** the note counts as internally marked

#### Scenario: A colon as delimiter is marked
- **WHEN** a booking carries one of the notes "Intern: JFX", "intern : JFX", "INTERN:JFX" or "  Intern: JFX"
- **THEN** the note counts as internally marked

#### Scenario: A marker in the middle of the text does not count
- **WHEN** a booking carries the note "JFX (Intern - Vorbereitung)" or "JFX (Intern: Vorbereitung)"
- **THEN** the note does not count as internally marked

#### Scenario: "Intern" without a dash does not count
- **WHEN** a booking carries the note "Internes Meeting" or "Intern JFX"
- **THEN** the note does not count as internally marked

#### Scenario: An empty note is not marked
- **WHEN** a booking carries no note or an empty note
- **THEN** the note does not count as internally marked
