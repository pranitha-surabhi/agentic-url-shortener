# Enhanced SDLC prototype — evidence and interview walkthrough

> **Prototype scope.** This branch adds actual deterministic Java artifact generation and validation, guarded reviewer approvals, restart-recoverable workflow snapshots, a hash-chained audit journal, and reliability-metric persistence. It does **not** fully automate modifications to the existing URL shortener using an LLM. It is not production-certified or an audit-grade compliance system.

## Prerequisites

- Java 21 JDK with the system Java compiler available
- Maven
- Local writable disk for `data/workflow-audit`, `data/workflow-state`, `data/workflow-metrics.properties`
- An independently chosen, sufficiently long reviewer token (at least 32 characters) in the environment variable `AGENTIC_REVIEWER_TOKEN`

No API keys or passwords should be committed to the repository.

## Run and verify

```bash
mvn -B verify
mvn spring-boot:run
```

The API defaults to port **9091** and the generated short URL uses `PUBLIC_BASE_URL` (or `http://localhost:9091` locally).

## Scenario 1 — Greenfield

```bash
curl -X POST "http://localhost:9091/api/v1/workflow/execute" \
  --data-urlencode "requirement=Build a new URL shortener with analytics"
```

Inspect `stageOutputs.engineeringEvidence`, `testing`, `documentation`, and the generated files under the system temporary directory's `agentic-url-shortener-artifacts/<workflow-id>` folder. The generator writes a trusted, deterministic URL policy source file, compiles it, and executes three acceptance checks.

## Scenario 2 — Brownfield with human approvals

```bash
curl -X POST "http://localhost:9091/api/v1/workflow/execute" \
  --data-urlencode "requirement=Modify existing URL shortener to support custom alias"
```

The workflow should enter `WAITING_FOR_APPROVAL`; check the architecture impact output and tasks. Use the returned `workflowId`:

```bash
curl -X POST "http://localhost:9091/api/v1/workflow/$WORKFLOW_ID/approve?stage=IMPLEMENTATION" \
  -H "X-Reviewer-Token: $AGENTIC_REVIEWER_TOKEN" \
  -H "X-Reviewer-Id: reviewer1"

curl -X POST "http://localhost:9091/api/v1/workflow/$WORKFLOW_ID/approve?stage=RELEASE_READINESS" \
  -H "X-Reviewer-Token: $AGENTIC_REVIEWER_TOKEN" \
  -H "X-Reviewer-Id: reviewer1"
```

Approvals are scoped to individual workflow stages; a high-impact replan invalidates earlier approvals. The shared-secret demo guard is fail-closed when unconfigured; use actual identity-provider integration before production use.

## Scenario 3 — Ambiguous requirement

```bash
curl -X POST "http://localhost:9091/api/v1/workflow/execute" \
  --data-urlencode "requirement=Make redirects faster"
```

Inspect the `clarificationQuestions` stage output. A real business requirement should not proceed past implementation approval until acceptance criteria are clarified.

## Reviewer-controlled operations

- `POST /api/v1/workflow/{workflowId}/approve?stage=IMPLEMENTATION` — scoped approval
- `POST /api/v1/workflow/{workflowId}/approve?stage=RELEASE_READINESS` — independent release review
- `POST /api/v1/workflow/{workflowId}/safe-stop?reason=...` — cooperative stop
- `POST /api/v1/workflow/replan` — a reviewer token is required for this high-impact action

## Evidence and constraints

1. `EngineeringEvidenceService` generates, compiles and runs tests against a deterministic Java template; it does not generate dynamic feature-specific patches to the application.
2. `WorkflowSnapshotCodec` stores typed stage outputs, task details and stage decisions for application restarts. Snapshot/journal updates are not a single atomic transaction; the disk must be persistent across redeploys.
3. `WorkflowAuditJournal` persists sequential timestamped, hash-chained audit messages. An administrator with file write access could rewrite the chain; this is not externally anchored evidence.
4. `WorkflowMetrics` stores aggregated counters locally; it does not replace centralized monitoring, tracing, or durable analytics pipelines.
5. Safe-stop is cooperative and acts at execution boundaries. It does not forcibly terminate a currently running JVM compilation or a child agent process.
6. Artifact rollback deletes generated temporary source and reports when a workflow fails. It does not undo edits to the existing repository or reverse database migrations.
7. Reviewer authentication uses a configured shared secret and supplied reviewer identifier, not enterprise SSO, MFA, OIDC claims or separation-of-duties enforcement.
8. The codebase impact view inspects loaded Java class/method signatures rather than performing a complete static dependency and data-flow analysis.
9. Release evidence is currently generated inside the application, not signed/verified against an external CI provider. A human must independently inspect CI, risk and change provenance before release.
10. Real agent-managed file patches, isolated untrusted-language-model execution, infrastructure rollback, fallback routing and comprehensive failure-injection coverage remain future engineering work.

## Acceptance before production claim

A release must have independent CI, trusted artifacts, authenticated accountable reviewers, authorized replan/stop, verified rollback, reliable backup/restore of persisted state, API authentication and rate limits, plus integration and concurrency test coverage. Passing Maven tests alone is insufficient.
