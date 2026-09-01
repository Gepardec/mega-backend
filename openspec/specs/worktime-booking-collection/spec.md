# Work Time Booking Collection

## Purpose

Defines the canonical, immutable collection representation of detailed time bookings within the `worktime` BC: its construction and ordering guarantees, the reusable domain views it exposes (by date, working-time-contributing, project, journey), its aggregate facts (booked dates, total duration), and the boundary that keeps warning decisions out of it.

## Requirements

### Requirement: Detailed bookings have an immutable canonical collection representation
The worktime domain SHALL provide a `WorkTimeBookings` value object for a collection of detailed `WorkTimeBooking` values. Construction SHALL reject a null collection or null booking, defensively copy the supplied values, and store them in stable chronological order by start time and then end time. Callers MUST NOT be able to mutate the value object's contents through either the original input collection or an exposed view.

#### Scenario: Unordered input becomes chronologically ordered
- **WHEN** `WorkTimeBookings` is constructed from bookings supplied in a non-chronological order
- **THEN** traversal returns the bookings ordered by start time and then end time

#### Scenario: Equal-time bookings retain stable input order
- **WHEN** multiple bookings have equal start and end times
- **THEN** their relative input order is retained in the canonical collection

#### Scenario: Input and exposed views cannot mutate the value object
- **WHEN** the source collection is changed after construction or a caller attempts to mutate the exposed booking view
- **THEN** the value object's contents remain unchanged and exposed mutation is rejected

#### Scenario: Null collection members are rejected
- **WHEN** construction receives a null collection or a collection containing a null booking
- **THEN** construction fails immediately with a null-related validation error

### Requirement: The collection exposes reusable worktime-domain views
`WorkTimeBookings` SHALL expose immutable, chronologically ordered views for bookings grouped by date, bookings contributing to working time, project bookings, and journey bookings. A booking contributes to working time when it is a non-journey task booking or a journey whose vehicle marks the employee as an active traveller. Date grouping SHALL retain chronological date order, and every grouped value SHALL itself be a `WorkTimeBookings` value object.

#### Scenario: Bookings are grouped into chronological daily collections
- **WHEN** bookings span multiple dates
- **THEN** grouping by date returns dates chronologically and each date maps to the canonically ordered bookings for that date

#### Scenario: Working-time view excludes inactive journeys
- **WHEN** the collection contains task bookings, active-traveller journeys, and inactive-traveller journeys
- **THEN** the working-time-contributing view contains the task bookings and active journeys but excludes inactive journeys

#### Scenario: Typed project and journey views preserve ordering
- **WHEN** the collection contains interleaved project and journey bookings
- **THEN** the project view contains only project bookings and the journey view contains only journey bookings, each in canonical chronological order

### Requirement: The collection exposes booking dates and exact aggregate duration
`WorkTimeBookings` SHALL expose its distinct booked dates in chronological encounter order and SHALL calculate total duration as a `Duration` equal to the sum of every contained booking's start-to-end duration. Empty collections SHALL report no dates and a zero duration.

#### Scenario: Booked dates are distinct and chronological
- **WHEN** multiple bookings occur on the same date and on later dates
- **THEN** the booked-date view contains each date once in chronological order

#### Scenario: Total duration sums all booking intervals
- **WHEN** the collection contains bookings with multiple durations
- **THEN** total duration equals the exact sum of the duration between each booking's start and end

#### Scenario: Empty collection has empty aggregate values
- **WHEN** `WorkTimeBookings` contains no bookings
- **THEN** it reports empty booking and date views, empty groups, and zero total duration

### Requirement: Warning calculators consume the canonical collection value object
Every detailed-booking warning calculator SHALL accept `WorkTimeBookings` instead of a raw booking list. The warning application flow SHALL construct the value object once after fetching detailed bookings and SHALL pass that same canonical collection through warning assembly to every booking-based calculator. Warning decisions, types, dates, quantities, and multiplicity SHALL remain equivalent to the pre-refactoring behaviour for equivalent booking content.

#### Scenario: Unordered provider input is canonicalised once
- **WHEN** the outbound provider returns detailed bookings in a non-chronological order
- **THEN** the warning application flow constructs one canonical `WorkTimeBookings` value and all booking-based calculators consume its chronological views

#### Scenario: Warning content remains behaviourally equivalent
- **WHEN** the migrated calculator parity scenarios are executed through `WorkTimeBookings`
- **THEN** every calculator produces the same warning types, dates, quantities, and occurrence counts as before the refactoring

### Requirement: The collection value object remains free of warning decisions
`WorkTimeBookings` SHALL provide collection facts and reusable worktime views without determining warning outcomes. Warning-specific validation and threshold logic SHALL remain outside the value object.

#### Scenario: Warning rules remain outside the collection value object
- **WHEN** `WorkTimeBookings` is inspected
- **THEN** it contains no warning types, warning thresholds, calendar rules, journey validation outcomes, or break/rest compliance decisions
