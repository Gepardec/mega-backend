## ADDED Requirements

### Requirement: Machine-to-machine monthend endpoints are grouped under a distinct contract tag
The contract SHALL group monthend endpoints secured by the internal client-credentials scheme under the `Cron` tag, distinct from every tag carried by actor-facing monthend endpoints, so that consumers can exclude them by tag without enumerating their paths. No endpoint reachable by an authenticated employee or project-lead session SHALL carry the `Cron` tag.

#### Scenario: Generation endpoint does not share a tag with actor-facing endpoints
- **WHEN** the contract is inspected for the tags carried by `POST /monthend/{month}/generate`
- **THEN** the endpoint carries the `Cron` tag
- **THEN** the endpoint carries no tag that any employee-facing or project-lead-facing monthend endpoint also carries

#### Scenario: A further client-credentials endpoint is added to the contract
- **WHEN** a monthend endpoint secured by the internal client-credentials scheme is added
- **THEN** the endpoint carries the `Cron` tag
- **THEN** the endpoint carries no tag that any employee-facing or project-lead-facing monthend endpoint also carries
