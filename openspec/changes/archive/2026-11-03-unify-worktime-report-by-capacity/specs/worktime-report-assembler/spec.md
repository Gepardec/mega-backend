## REMOVED Requirements

### Requirement: Domain service encapsulates worktime hour aggregation
**Reason**: Hour aggregation and entry construction are part of the work time report model itself rather than a separate domain service. The capability only described how the code was structured; the observable behavior is specified in `worktime-report`.
**Migration**: See `worktime-report`. Billable and non-billable hours per (employee x project) and `employeeMonthTotalHours` across all of an employee's projects are covered by its per-employee and per-lead requirements.

### Requirement: Both worktime use cases delegate assembly to WorkTimeReportAssembler
**Reason**: The two work time use cases are merged into one capacity-parameterized report, and assembly no longer goes through a separate domain service.
**Migration**: See `worktime-report`, which defines the same aggregation for both capacities.
