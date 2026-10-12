## ADDED Requirements

### Requirement: The worktime BC exposes employee absences as a formal use case
The system SHALL provide `GetEmployeeAbsencesUseCase` in the `worktime` BC returning `List<Absence>` for a given `UserId` and `YearMonth`. Each `Absence` SHALL carry a `LocalDate` (the absence date) and an `AbsenceType`. The use case SHALL resolve the employee's ZEP username via `WorkTimeUserSnapshotPort` before delegating to the ZEP outbound port. If no user is found for the given `UserId` and month, the use case SHALL throw `WorkTimeUserNotFoundException`.

#### Scenario: Absences are returned for a known employee
- **WHEN** `GetEmployeeAbsencesUseCase.getAbsences(employeeId, month)` is called for an employee with ZEP absences in that month
- **THEN** the result contains one `Absence` per absence day with the correct `AbsenceType`

#### Scenario: Empty list returned when employee has no absences
- **WHEN** `GetEmployeeAbsencesUseCase.getAbsences(employeeId, month)` is called for an employee with no ZEP absences in that month
- **THEN** the result is an empty list

#### Scenario: Unknown employee raises WorkTimeUserNotFoundException
- **WHEN** `GetEmployeeAbsencesUseCase.getAbsences(employeeId, month)` is called for a UserId not active in that month
- **THEN** `WorkTimeUserNotFoundException` is thrown

### Requirement: AbsenceType enum covers all ZEP absence categories
The system SHALL define an `AbsenceType` enum in `worktime/domain/model` with values mirroring the legacy `com.gepardec.mega.db.entity.common.AbsenceType` enum: `VACATION` (UB), `PAID_SICK_LEAVE` (KR), `HOME_OFFICE` (HO), `EXTERNAL_TRAINING` (EW), `MATERNITY_LEAVE` (KA), `NURSING` (PU), `COMPENSATORY_TIME_OFF` (FA), `CONFERENCE` (KO), `MATERNITY_PROTECTION` (MU), `FATHER_MONTH` (PA), `PAID_SPECIAL_LEAVE` (SU), `NON_PAID_VACATION` (UU). There is no `OTHER` fallback — absences with unrecognised ZEP codes are silently skipped.

#### Scenario: ZEP absence type is mapped to AbsenceType
- **WHEN** the `WorkTimeAbsenceZepAdapter` processes a raw ZEP absence record with a known code
- **THEN** the resulting `Absence` has the correct `AbsenceType` value

#### Scenario: Unknown ZEP absence codes are skipped
- **WHEN** the `WorkTimeAbsenceZepAdapter` processes a ZEP absence record with an unrecognised code
- **THEN** no `Absence` is produced for that record
