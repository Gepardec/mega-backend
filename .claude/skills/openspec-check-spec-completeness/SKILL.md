---
name: openspec-check-spec-completeness
description: Proactively verify after a change proposal is created that every existing capability touched by the change has a corresponding delta spec file. Surfaces gaps the proposal's Modified Capabilities list may have missed.
user-invocable: false
---

Run this check proactively after /opsx:propose completes.

1. Read the change's `proposal.md`. Collect every capability name listed under **Modified Capabilities**.

2. For each, verify that a delta spec exists at `openspec/changes/<change>/specs/<capability>/spec.md`.

3. Scan `design.md` and `tasks.md` for mentions of any name that matches an existing folder under `openspec/specs/` that is NOT already listed in Modified Capabilities.

4. If any gaps are found, surface them clearly:
   > "The following existing capabilities appear to be affected but have no delta spec: `<list>`. Should we add them?"

5. If no gaps are found, say nothing — do not produce output for a passing check.
