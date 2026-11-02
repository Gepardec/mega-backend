## ADDED Requirements

### Requirement: A note counts as internally marked when it starts with the word "Intern"
A note SHALL count as internally marked if, after optional leading spaces, it starts with the word "Intern", regardless of upper or lower case, directly followed by one or more separators: whitespace, a hyphen (`-`), an en dash (`–`) or a colon (`:`). After the separators, further text SHALL follow. An empty or missing note SHALL NOT count as internally marked, nor SHALL a note that contains the marker only elsewhere.

#### Scenario: A note following the convention is marked
- **WHEN** a booking carries the note "Intern - Jenkins aufräumen"
- **THEN** the note counts as internally marked

#### Scenario: Deviating spellings are marked
- **WHEN** a booking carries one of the notes "intern - JFX", "Intern -JFX", "Intern– JFX", "INTERN-JFX" or "  Intern - JFX"
- **THEN** the note counts as internally marked

#### Scenario: A colon as delimiter is marked
- **WHEN** a booking carries one of the notes "Intern: JFX", "intern : JFX", "INTERN:JFX" or "  Intern: JFX"
- **THEN** the note counts as internally marked

#### Scenario: "Intern" followed by a space without a delimiter is marked
- **WHEN** a booking carries the note "Intern JFX"
- **THEN** the note counts as internally marked

#### Scenario: Internal project names as prefix are marked
- **WHEN** a booking carries one of the notes "Intern LF-Projekte - MEGA", "Intern-Jagdthemen - Workshop" or "Intern-Weiterbildungen: Java Kurs"
- **THEN** the note counts as internally marked

#### Scenario: A marker in the middle of the text does not count
- **WHEN** a booking carries the note "JFX (Intern - Vorbereitung)" or "JFX (Intern: Vorbereitung)"
- **THEN** the note does not count as internally marked

#### Scenario: A word that merely starts with "Intern" does not count
- **WHEN** a booking carries the note "Internes Meeting" or "Internal sync"
- **THEN** the note does not count as internally marked

#### Scenario: "Intern" followed by another character does not count
- **WHEN** a booking carries the note "Intern/Extern abstimmen" or "Intern.JFX"
- **THEN** the note does not count as internally marked

#### Scenario: "Intern" without further text does not count
- **WHEN** a booking carries one of the notes "Intern", "Intern -", "Intern: " or "intern  "
- **THEN** the note does not count as internally marked

#### Scenario: An empty note is not marked
- **WHEN** a booking carries no note or an empty note
- **THEN** the note does not count as internally marked

## REMOVED Requirements

### Requirement: A note counts as internally marked when it starts with "Intern" and a delimiter
**Reason**: The delimiter requirement rejected common notes such as "Intern JFX" and "Intern LF-Projekte - MEGA". It is replaced by "A note counts as internally marked when it starts with the word "Intern"".
**Migration**: None needed. Every note that counted as marked before still counts, except notes made up only of "Intern" and a delimiter (e.g. "Intern -"), which now count as unmarked.
