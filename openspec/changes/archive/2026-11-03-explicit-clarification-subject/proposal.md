## Why

`POST /monthend/clarifications` guesses what an omitted `subjectEmployeeId` means from the caller's relationship to the project. An eligible lead gets a project-level clarification, and anyone else becomes the subject. For a caller who leads a project and also works on it, the guess depends on the role instead of on what they meant. On their employee dashboard they want a clarification about their own hours, but the server creates a project-level one. This is the same mistake of letting the role stand in for the view that `unify-monthend-reads-by-capacity` removes from the reads. Commands should carry explicit intent instead of having the server infer it.

## What Changes

- **BREAKING (behavior):** leaving out `subjectEmployeeId` (or sending `null`) always means a **project-level** clarification. Only an eligible project lead of the requested project in the requested month may create one. Any other caller is rejected with `403 Forbidden`, and the server no longer turns the caller into the subject.
- To create a clarification about themselves, every caller sends their own id as `subjectEmployeeId`. This includes leads acting on their own employee page.
- Unchanged: an eligible lead may name any project employee as subject, a non-lead may name only themselves, and naming another user as a non-lead is rejected with `403`.
- The contract describes the meaning of an omitted subject explicitly. The request schema itself does not change.

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `monthend-rest-api`: the clarification creation requirement no longer derives the subject from lead eligibility when `subjectEmployeeId` is omitted. An omitted subject always means project-level and requires lead eligibility.

`monthend-clarifications` already states that a project-level clarification must be created by an eligible project lead, and it does not change. This change makes the REST behavior match that rule instead of working around it.

## Impact

- **API contract:** no schema change. The `subjectEmployeeId` description and the operation description say what omission means. The frontend must send `subjectEmployeeId` for clarifications about the caller (employee dashboard). Otherwise the request becomes project-level for leads and is rejected for everyone else. Frontend work is out of scope for this change.
- **Application:** subject resolution in the clarification creation use case loses its "omitted, so the subject is the caller" branch.
- **Unchanged:** the clarification aggregate and its creator validation, ZEP-mail and system-created clarifications (which do not go through this endpoint), and edit, resolve and delete.
- **Sequencing:** apply after `unify-monthend-reads-by-capacity`. That change removes the overview requirement's clarification-creation scenario, which would otherwise contradict this change in the main spec.
