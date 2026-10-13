# Design

## Context

All four ZEP REST client interfaces in `com.gepardec.mega.zep.rest.client` carry the same class-level fault-tolerance pair:

```java
@RateLimit(value = 1000, window = 5, windowUnit = ChronoUnit.MINUTES, type = RateLimitType.ROLLING)
@Retry(delay = 1000, retryOn = RateLimitException.class)
```

- A class-level fault-tolerance annotation keeps separate state for each method, so the limiter neither models ZEP's (presumably per-token) quota nor matches it: the attendance client alone allows 3 × 1000 requests, every other client adds its own, and every replica counts separately.
- `@Retry` uses the default `maxRetries = 3`, so it gives up after ~3–4 s while a full rolling 5-minute window can take minutes to free a slot. The `RateLimitException` then reaches `ApplicationExceptionMapper` as an HTTP 500.
- No request sets a page size, so ZEP uses its default `per_page: 15`. Pagination is driven by `links.next` (`ZepResponse.hasNext()`); the services loop over pages until it is empty.
- These are the only usages of SmallRye Fault Tolerance in `src/main/java`.
- All ZEP REST response types in `com.gepardec.mega.zep.rest.dto` already ignore unknown fields (`@JsonIgnoreProperties(ignoreUnknown = true)` on every type). The same annotation on the `ZepAttendanceRestClient`, `ZepEmployeeRestClient` and `ZepProjectRestClient` interfaces has no effect, because Jackson reads it from the deserialized type, not from a JAX-RS client interface.

Probe findings (2026-10-13, ZEP test instance, script in the appendix): 1200 requests in 14 s with 20 in parallel all returned 200. Neither success nor error responses carried `X-RateLimit-*` or `Retry-After` headers.

## Goals / Non-Goals

**Goals:**
- Fewer ZEP requests per use case through the maximum page size.
- No failures caused by MEGA's own rate limiter.
- Client method signatures stay unchanged, so callers and the tests that mock the clients keep working.

**Non-Goals:**
- Handling HTTP 429 (backoff, `Retry-After`). Deferred until production shows ZEP throttles (trial and error).
- Bounding concurrency in the Mutiny fan-outs.
- Reducing request counts by changing query granularity (e.g. per project instead of per employee) or caching.
- Changing `getProjectByName`: it returns a list by the nature of the endpoint, but a name match yields a single project, so it remains unpaginated.

## Decisions

### `limit` as a class-level `@ClientQueryParam` backed by config
Each client interface gets `@ClientQueryParam(name = "limit", value = "${mega.zep.page-size}")`, with `mega.zep.page-size: 200` in `application.yaml`.

- **Alternative: a `@QueryParam("limit")` method parameter.** Rejected: it changes every method signature, forces every caller to pass the same constant, and breaks every Mockito stub of these clients in the tests.
- **Alternative: a hard-coded `value = "200"`.** Rejected: the page size would be repeated four times; the config value keeps the single place the spec asks for.
- The parameter is also sent to single-resource endpoints (`getByUsername`, `getProjectById`, `getAttachmentForReceipt`). ZEP ignores unknown or irrelevant query parameters there; verified during implementation with a smoke call.

### Remove `@RateLimit` and `@Retry` entirely, and the fault-tolerance extension with them
The retry only retries on `RateLimitException`, so it is meaningless without the limiter. With both gone, `quarkus-smallrye-fault-tolerance` has no usage left; drop the dependency and the `quarkus.fault-tolerance` config block rather than keeping an unused extension.

- **Alternative: fix the limiter instead** (one shared limiter, `maxDuration` covering the window). Rejected: the probe shows no throttling to protect against, and waiting up to 5 minutes inside a user request is not acceptable either.

### No code change for unknown-field tolerance; remove the ineffective interface annotations
The requirement already holds through the response types. The `@JsonIgnoreProperties` on three client interfaces is removed, because it suggests a behavior it does not provide.

- **Alternative: add the annotation to `ZepReceiptRestClient` as well**, for consistency. Rejected: it would also have no effect and spread the misconception.

## Risks / Trade-offs

- [Production ZEP enforces the documented 1000 / 5 min while the test instance does not] → Failures surface as ZEP errors in the log (status code visible); add 429 handling then. The larger page size already cuts the request volume 3–6×.
- [A page of 200 entries makes individual responses larger and slower] → Still small payloads (a month of attendances for one employee); acceptable.
- [ZEP rejects `limit` on single-resource endpoints] → Covered by the smoke call during implementation; if it happens, move `@ClientQueryParam` from class level to the paginated methods only.

## Migration Plan

No data migration. Deploy normally; rollback is a revert of the commit.

## Appendix: ZEP rate-limit probe script

Removed from the repository and kept here for reuse. Save as `scripts/zep-rate-limit-probe.sh`, `chmod +x`, then run `ZEP_REST_TOKEN=... scripts/zep-rate-limit-probe.sh [total-requests] [parallelism]`. Every request counts against the ZEP quota of the targeted instance.

```bash
#!/usr/bin/env bash
#
# Fires a burst of parallel requests against the ZEP REST API to find out
# whether (and when) the documented rate limit of 1000 requests / 5 minutes kicks in.
#
# Usage:
#   ZEP_REST_TOKEN=... scripts/zep-rate-limit-probe.sh [total-requests] [parallelism]
#
# Defaults: 1200 requests, 20 in parallel.
# Prints a count per HTTP status code and the headers of the first non-200 response.

set -euo pipefail

: "${ZEP_REST_TOKEN:?ZEP_REST_TOKEN must be set}"

TOTAL="${1:-1200}"
PARALLEL="${2:-20}"
ZEP_ORIGIN="${ZEP_ORIGIN:-https://www.zep-online.de/zepgepardecservices_test}"
URL="${ZEP_ORIGIN}/next/api/v1/attendances?start_date=2026-09-01&end_date=2026-09-30"

OUT_DIR="$(mktemp -d)"
export ZEP_REST_TOKEN URL OUT_DIR

echo "Sending ${TOTAL} requests (${PARALLEL} in parallel) to ${URL}"
START=$(date +%s)

# The request number is passed as $1 instead of via `xargs -I{}`, because BSD xargs (macOS)
# silently skips the substitution once the resulting argument exceeds 255 bytes.
seq 1 "${TOTAL}" | xargs -P "${PARALLEL}" -n 1 bash -c '
  code=$(curl -s -o /dev/null -D "${OUT_DIR}/headers-$1.txt" -w "%{http_code}" \
    -H "Authorization: Bearer ${ZEP_REST_TOKEN}" "${URL}")
  echo "${code}" > "${OUT_DIR}/status-$1.txt"
  [ "${code}" = "200" ] && rm -f "${OUT_DIR}/headers-$1.txt"
  true
' _

END=$(date +%s)
echo "Done in $((END - START))s"
echo
echo "Status code counts:"
cat "${OUT_DIR}"/status-*.txt | sort | uniq -c

FIRST_FAILURE=$(cd "${OUT_DIR}" && ls headers-*.txt 2>/dev/null | sort -t- -k2 -n | head -1 || true)
if [ -n "${FIRST_FAILURE}" ]; then
  echo
  echo "Headers of first non-200 response (${FIRST_FAILURE}):"
  cat "${OUT_DIR}/${FIRST_FAILURE}"
fi

rm -rf "${OUT_DIR}"
```
