# Proposal

## Why

Production requests fail with HTTP 500 because MEGA's own client-side rate limiter for the ZEP REST API rejects calls (`RateLimitException`), and the attached retry gives up after ~3 seconds while the limiter's rolling window spans 5 minutes. A probe against the ZEP test instance sent 1200 requests in 14 seconds without a single rejection, so the limiter guards against a throttling we have never observed. At the same time, every paginated call uses ZEP's default page size of 15, multiplying the number of requests per employee and month by 3–6.

## What Changes

- Remove client-side rate limiting and the rate-limit retry from all ZEP REST clients (attendances, employees, projects, receipts). Requests are sent to ZEP as soon as they are issued.
- Request the maximum page size ZEP supports (200 entries) on every ZEP REST request, configurable in one place.
- Record as a requirement that MEGA tolerates unknown fields in all ZEP REST responses. Every response type already behaves this way; no code change is needed. The ineffective unknown-field annotations on the client interfaces are removed.
- No dedicated handling for HTTP 429 from ZEP: if ZEP ever throttles, the failure surfaces like any other ZEP error and is addressed then.
- The ad-hoc rate-limit probe script is not kept in the repository; its content is preserved in `design.md` for later reuse.

## Capabilities

### New Capabilities
- `zep-rest-integration`: how MEGA issues requests to the ZEP REST API — page size, request pacing, and tolerance of response shape.

### Modified Capabilities
<!-- none: no existing spec states requirements about ZEP request pacing or page size -->

## Impact

- Code: `com.gepardec.mega.zep.rest.client` (`ZepAttendanceRestClient`, `ZepEmployeeRestClient`, `ZepProjectRestClient`, `ZepReceiptRestClient`); configuration in `application.yaml`.
- Callers (legacy `zep.rest.service` and hexagon outbound adapters using them) are unchanged — client method signatures stay the same.
- External system: ZEP receives fewer, larger requests; MEGA no longer self-throttles.
- Risk: if production ZEP enforces the documented 1000 requests / 5 minutes, requests beyond it will fail with ZEP's error instead of MEGA's — accepted as trial and error.
