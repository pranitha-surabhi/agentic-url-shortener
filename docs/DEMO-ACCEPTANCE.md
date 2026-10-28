# Charles Schwab prototype: acceptance and evidence gates

This checklist intentionally distinguishes **implemented**, **automated-test verified**,
**live-run verified**, and **production hardened**. A CI green build alone does not prove 25/25.

| # | Acceptance evidence needed |
|---|---|
| 1 | Ambiguous requirement interrupts for human clarification and resumes with revised criteria |
| 2 | Tasks include dependencies and sequence |
| 3 | Brownfield code impact covers affected source, tests, APIs, and data flows |
| 4 | Explicit directed acyclic workflow graph |
| 5 | Sequential dependency execution |
| 6 | Parallel independent nodes |
| 7 | Barrier waits for prerequisite stages |
| 8 | Restart restores durable workflow state |
| 9 | Context is passed between stages |
| 10 | Trace contains decisions and relevant source/test artifact hashes |
| 11 | Approval is tied to the exact patch; declined or tampered approval never modifies workspace |
| 12 | Retried operation stops at a configured maximum |
| 13 | Failed/rejected execution follows explicit safe fallback and requires manual review |
| 14 | Failure cleans up isolated workspace and restores state |
| 15 | Safe-stop prevents further stage execution |
| 16 | Restricted file paths, no secret leakage, and approval before editing |
| 17 | Recorded audit events detect unauthorized modification |
| 18 | Counters include success, retry, rollback, latency and recovery time |
| 19 | Upstream change invalidates downstream artifacts and approvals |
| 20 | Live model generates requirement-specific Java source diff |
| 21 | Java Maven verification runs on the generated patch inside a restricted runner |
| 22 | Generated summary references actual engineering evidence |
| 23 | Risk analysis lists mitigations, assumptions and tradeoffs |
| 24 | AI cannot bypass approval or edit outside allowed sandbox |
| 25 | Final summary includes requirement, plan, diff, validation, review decision, and limitations |

## Current blockers

- Live LLM + sandbox Maven test needs an environment-held API key and manual workflow execution.
- The current Python graph is a minimal agent workflow and does not provide full source/data-flow reasoning, automated replanning, or CI provenance for every approval.
- The Java graph contains retry, rollback, safe-stop, metrics and stage synchronization, but lacks comprehensive failure-injection tests.
- Local file audit logs and shared-secret reviewers should not be represented as enterprise audit-grade controls.

## Demonstration checklist

1. Run `mvn -B verify` and Python unit tests.
2. Start a greenfield scenario, collect generated source, acceptance report, and final summary.
3. Start a brownfield scenario, inspect impacted files and verify stage approvals.
4. Start an ambiguous scenario and show clarification before any patch approval.
5. Run a negative approval test with an invalid patch digest.
6. Run `.github/workflows/agentic-live-verification.yml` with a protected GitHub environment and capture the archived JSON report.
7. Record the current git SHA and exact passing workflow URLs in the final submission.

**Status policy:** Do not claim 25/25 until all of these acceptance checks are supported by reproducible run evidence.
