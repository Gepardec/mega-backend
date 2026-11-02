# Tasks

## 1. Relax the internal note marker

- [x] 1.1 In `InternalNoteConventionCalculatorTest`, update the note `@ValueSource` lists to match the spec: add "Intern JFX", "Intern LF-Projekte - MEGA", "Intern-Jagdthemen - Workshop" and "Intern-Weiterbildungen: Java Kurs" to both marked-note lists (billable → `INTERNAL_NOTE_BILLABLE`, non-billable → no warning); remove "Intern JFX" from the not-marked list and add "Internal sync", "Intern/Extern abstimmen", "Intern.JFX", "Intern", "Intern -", "Intern: " and "intern  " to it. Verify the newly marked cases and the bare-"Intern" cases fail with `mvn test -Dtest=InternalNoteConventionCalculatorTest`
- [x] 1.2 Replace `INTERNAL_MARKER` in `InternalNoteConventionCalculator` with `^\s*intern[\s\-–:]+[^\s\-–:]` (keeping `CASE_INSENSITIVE`, as described in design.md). Verify `mvn test -Dtest=InternalNoteConventionCalculatorTest` passes
- [x] 1.3 Verify that the other tests using internal notes ("Intern - JFX", "Intern - Test", "Intern - Reisepauschale") still pass: `mvn test -Dtest='WorkTimeWarningAssemblerTest,GetEmployeeWarningsServiceTest,WorkTimeBookingZepMapperTest'`

## 2. Code hygiene

- [x] 2.1 Run `mvn package -Prewrite-check`. If it reports drift, run `mvn rewrite:run` and re-run the check until the build passes
