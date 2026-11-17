## ADDED Requirements

### Requirement: A project can declare its own travel-allowance task
A project SHALL declare its own travel-allowance task when ZEP lists a task named "Reisepauschale" for it, compared ignoring letter case and leading or trailing whitespace. Every task with that name SHALL count. The system SHALL read a project's tasks as they are when the warnings are requested and apply them to every day of the month. A project ZEP lists no such task for, and the project "Intern", SHALL declare none.

#### Scenario: A task named "Reisepauschale" is the project's own task
- **WHEN** ZEP lists a task "Reisepauschale" for project X
- **THEN** project X declares that task as its own travel-allowance task

#### Scenario: Letter case and surrounding whitespace are ignored
- **WHEN** ZEP lists a task " reisepauschale " for project X
- **THEN** project X declares that task as its own travel-allowance task

#### Scenario: A longer task name is not recognised
- **WHEN** ZEP lists only a task "Reisepauschale Kunde" for project X
- **THEN** project X declares no own travel-allowance task

#### Scenario: A task created later in the month applies to the whole month
- **WHEN** the task "Reisepauschale" of project X was created on 2026-11-15 and the warnings for November 2026 are requested afterwards
- **THEN** project X declares its own travel-allowance task for every day of November 2026, including the days before 2026-11-15

#### Scenario: A project ZEP does not know declares no own task
- **WHEN** a passive journey refers to a ZEP project for which ZEP lists no tasks because it does not know the project
- **THEN** the project declares no own travel-allowance task and the warnings request does not fail

### Requirement: A journey's allowance belongs on its project's own task or on "Intern"
The allowance a passive journey permits SHALL belong on the own travel-allowance task of the journey's project when that project declares one, and on the process "Reisepauschale" of the project "Intern" otherwise. The project's billability SHALL NOT matter.

#### Scenario: A journey on a project with its own task
- **WHEN** a passive journey is booked on project X, which declares its own travel-allowance task
- **THEN** the journey's allowance belongs on that task of project X

#### Scenario: A journey on a billable project without its own task
- **WHEN** a passive journey is booked on a billable project that declares no own travel-allowance task
- **THEN** the journey's allowance belongs on "Intern"

#### Scenario: A journey on "Intern" or without project
- **WHEN** a passive journey is booked on "Intern", on a project unknown to the system, or without project
- **THEN** the journey's allowance belongs on "Intern"

### Requirement: Travel allowance exceeding the journeys' allowance is reported per day
The system SHALL produce a `TRAVEL_ALLOWANCE_EXCEEDED` warning for every date on which the allowance booked at a place ("Intern" or a project's own task) exceeds what that day's passive journeys belonging there permit. A place without such journeys permits 0. The warning SHALL carry the sum of all places' excesses in hours, rounded to two decimals. An excess on "Intern" SHALL NOT count on a day with `TRAVEL_ALLOWANCE_WRONG_PROJECT`.

#### Scenario: Travel allowance within the allowance is not reported
- **WHEN** a day with a train journey from Wien to Linz and back on "Intern" carries 60 minutes on the process "Reisepauschale" of "Intern"
- **THEN** no `TRAVEL_ALLOWANCE_EXCEEDED` warning is produced for that day

#### Scenario: Travel allowance above the allowance is reported with the excess
- **WHEN** a day with a train journey from St. Pölten to Wien on "Intern" carries 30 minutes on the process "Reisepauschale" of "Intern"
- **THEN** a `TRAVEL_ALLOWANCE_EXCEEDED` warning for that day with `hours` 0.25 is produced

#### Scenario: Travel allowance without any journey is reported
- **WHEN** a day without journeys carries 30 minutes on the process "Reisepauschale" of "Intern"
- **THEN** a `TRAVEL_ALLOWANCE_EXCEEDED` warning for that day with `hours` 0.5 is produced

#### Scenario: Travel allowance on a day with only active journeys is reported
- **WHEN** a day whose only journeys are driven by the employee carries 30 minutes on the process "Reisepauschale" of "Intern"
- **THEN** a `TRAVEL_ALLOWANCE_EXCEEDED` warning for that day with `hours` 0.5 is produced

#### Scenario: A trip on a billable project without its own task permits allowance on "Intern"
- **WHEN** a day with a train journey from Wien to Linz and back on a billable project without own travel-allowance task carries 60 minutes on the process "Reisepauschale" of "Intern"
- **THEN** no travel-allowance warning is produced for that day

#### Scenario: Travel allowance on a project's own task within the allowance is not reported
- **WHEN** a day with a train journey from Wien to Linz and back on project X carries 60 minutes on the own travel-allowance task of project X
- **THEN** no travel-allowance warning is produced for that day

#### Scenario: Travel allowance on a project's own task above the allowance is reported
- **WHEN** a day with a train journey from Wien to Linz and back on project X carries 90 minutes on the own travel-allowance task of project X
- **THEN** a `TRAVEL_ALLOWANCE_EXCEEDED` warning for that day with `hours` 0.5 is produced

#### Scenario: Travel allowance on a project's own task without a journey on that project is reported
- **WHEN** a day with a train journey from Wien to Linz on "Intern" carries 30 minutes on the process "Reisepauschale" of "Intern" and 30 minutes on the own travel-allowance task of project X
- **THEN** a `TRAVEL_ALLOWANCE_EXCEEDED` warning for that day with `hours` 0.5 is produced

#### Scenario: Excesses at several places add up
- **WHEN** a day without journeys carries 30 minutes on the process "Reisepauschale" of "Intern" and 15 minutes on the own travel-allowance task of project X
- **THEN** exactly one `TRAVEL_ALLOWANCE_EXCEEDED` warning for that day with `hours` 0.75 is produced

### Requirement: Travel allowance on "Intern" for a project with its own task is reported per day
The system SHALL produce a `TRAVEL_ALLOWANCE_WRONG_PROJECT` warning for every date on which the allowance booked on "Intern" exceeds what that day's passive journeys belonging on "Intern" permit, and at least one passive journey of that day belongs on a project's own travel-allowance task. The warning SHALL carry the date and no hours.

#### Scenario: Travel allowance on "Intern" for a trip on a project with its own task is reported
- **WHEN** a day with a train journey from Wien to Linz and back on project X, which declares its own travel-allowance task, carries 60 minutes on the process "Reisepauschale" of "Intern"
- **THEN** a `TRAVEL_ALLOWANCE_WRONG_PROJECT` warning for that day without hours is produced and no `TRAVEL_ALLOWANCE_EXCEEDED` warning for that day

#### Scenario: A mixed day with too much on "Intern" is reported as wrong project
- **WHEN** a day with a train journey from Wien to Linz on "Intern" and a train journey back on project X, which declares its own travel-allowance task, carries 60 minutes on the process "Reisepauschale" of "Intern"
- **THEN** a `TRAVEL_ALLOWANCE_WRONG_PROJECT` warning for that day is produced and no `TRAVEL_ALLOWANCE_EXCEEDED` warning for that day

#### Scenario: A mixed day booked on both places is not reported
- **WHEN** a day with a train journey from Wien to Linz on "Intern" and a train journey back on project X, which declares its own travel-allowance task, carries 30 minutes on the process "Reisepauschale" of "Intern" and 30 minutes on the own travel-allowance task of project X
- **THEN** no travel-allowance warning is produced for that day

#### Scenario: A day before the task was created is reported as well
- **WHEN** project X created its own travel-allowance task on 2026-11-15 and 2026-11-03 carries a train journey from Wien to Linz on project X and 30 minutes on the process "Reisepauschale" of "Intern"
- **THEN** a `TRAVEL_ALLOWANCE_WRONG_PROJECT` warning for 2026-11-03 is produced

#### Scenario: A trip on a project without its own task is not reported as wrong project
- **WHEN** a day with a passive journey on a billable project without own travel-allowance task carries 30 minutes on the process "Reisepauschale" of "Intern"
- **THEN** no `TRAVEL_ALLOWANCE_WRONG_PROJECT` warning is produced for that day

## MODIFIED Requirements

### Requirement: Travel-allowance bookings are bookings on the process "Reisepauschale"
A booking SHALL be a travel-allowance booking when it is booked on the ZEP process "Reisepauschale" of the project "Intern" (process id 873) or on a project's own travel-allowance task. The travel allowance booked on a day SHALL be determined per place, as the total duration of that day's travel-allowance bookings on "Intern" and on each project's own travel-allowance task respectively. Bookings on other processes SHALL NOT count.

#### Scenario: Several travel-allowance bookings on a day add up
- **WHEN** a day carries two bookings of 15 minutes each on the process "Reisepauschale" of "Intern"
- **THEN** the travel allowance booked on "Intern" on that day is 30 minutes

#### Scenario: A booking on a project's own travel-allowance task counts
- **WHEN** a day carries a booking of 30 minutes on the own travel-allowance task of project X
- **THEN** the travel allowance booked on project X on that day is 30 minutes and the travel allowance booked on "Intern" is 0 minutes

#### Scenario: Bookings on other processes do not count
- **WHEN** a day carries a non-billable booking of 30 minutes on a task of a customer project that is not its own travel-allowance task, and no other booking
- **THEN** no travel allowance is booked on that day and no travel-allowance warning is produced

## REMOVED Requirements

### Requirement: Journeys on billable projects are customer trips
**Reason**: Billability no longer decides where a journey's allowance belongs. A project that tracks travel time declares its own "Reisepauschale" task. Every other journey's allowance belongs on "Intern", including journeys on billable projects.
**Migration**: Replaced by "A project can declare its own travel-allowance task" and "A journey's allowance belongs on its project's own task or on "Intern"". The travel-allowance rule no longer reads project billability.

### Requirement: Travel allowance exceeding the internal trips' allowance is reported per day
**Reason**: The excess is now checked per place ("Intern" and each project's own task), not only against internal trips.
**Migration**: Replaced by "Travel allowance exceeding the journeys' allowance is reported per day". The `TRAVEL_ALLOWANCE_EXCEEDED` identifier and its `hours` value are unchanged.

### Requirement: Travel allowance on "Intern" for a customer trip is reported per day
**Reason**: Allowance on "Intern" for a trip on a billable project without its own task is now correct.
**Migration**: Replaced by "Travel allowance on "Intern" for a project with its own task is reported per day", which keeps the identifier `TRAVEL_ALLOWANCE_WRONG_PROJECT` with the new meaning. The frontend changes the hint text for this type.
