# zep-rest-integration Specification

## Purpose
Defines how MEGA issues requests to the ZEP REST API: which page size it requests, whether it paces its own requests, and how it tolerates changes in ZEP's response shape.

## Requirements

### Requirement: ZEP REST requests use the maximum page size
Every request MEGA sends to the ZEP REST API SHALL ask for the maximum page size ZEP supports (currently 200 entries per page) via the `limit` query parameter. The page size SHALL be configurable in a single place.

#### Scenario: Paginated list request asks for 200 entries
- **WHEN** MEGA requests attendances for an employee and month from ZEP
- **THEN** the request to ZEP carries `limit=200` alongside the `page` parameter

#### Scenario: Results spanning several pages are still read completely
- **WHEN** ZEP reports a further page via `links.next`
- **THEN** MEGA requests the next page with `limit=200` until `links.next` is empty and returns the entries of all pages

### Requirement: MEGA does not throttle its own ZEP REST requests
MEGA SHALL send each ZEP REST request when it is issued, without a client-side request quota, and SHALL NOT fail a request because of a self-imposed rate limit. Errors returned by ZEP itself, including throttling responses, SHALL surface as ZEP errors.

#### Scenario: Burst above the documented ZEP quota is sent
- **WHEN** MEGA issues more than 1000 ZEP REST requests within 5 minutes
- **THEN** every request is sent to ZEP and none is rejected by MEGA before reaching ZEP

#### Scenario: ZEP throttles a request
- **WHEN** ZEP answers a request with HTTP 429 Too Many Requests
- **THEN** the request fails with that ZEP error and is not retried by MEGA

### Requirement: ZEP REST responses tolerate unknown fields
MEGA SHALL ignore fields in ZEP REST responses that it does not use, for every ZEP REST resource it reads, so that ZEP adding fields to a response does not break MEGA.

#### Scenario: Response contains an unknown field
- **WHEN** ZEP returns a response from any REST resource MEGA reads, and the JSON contains a field MEGA does not know
- **THEN** MEGA reads the response successfully and ignores that field
