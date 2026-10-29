# Tasks

## 1. Accept colon as marker delimiter

- [x] 1.1 In `InternalNoteConventionCalculatorTest`, add "Intern: JFX", "intern : JFX", "INTERN:JFX" and "  Intern: JFX" to the marked-note `@ValueSource` lists (billable → `INTERNAL_NOTE_BILLABLE`, non-billable → no warning), remove "Intern: JFX" from the not-marked list, and add "JFX (Intern: Vorbereitung)" to the not-marked list; verify the new colon cases fail with `mvn test -Dtest=InternalNoteConventionCalculatorTest`
- [x] 1.2 Extend `INTERNAL_MARKER` in `InternalNoteConventionCalculator` from `[-–]` to `[-–:]`; verify `mvn test -Dtest=InternalNoteConventionCalculatorTest` passes

## 2. Code hygiene

- [x] 2.1 Run `mvn package -Prewrite-check`; if it reports drift, run `mvn rewrite:run` and re-run the check until the build passes
