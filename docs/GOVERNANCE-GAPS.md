# Governance hardening: evidence and remaining work

## Change in this branch

The workflow execution API previously accepted `approvalGranted=true` and forwarded it directly to the workflow engine. A requesting client could therefore designate itself as the approving human. This change rejects that input with HTTP 403 and retains default execution behavior.

A regression test verifies that the workflow service is not called when the client attempts to self-approve. A CI verification workflow is added for Java 21.

## Boundary and limitations

**This is not yet a complete human approval implementation.** Brownfield and ambiguous workflows pause at their approval checkpoint; they cannot be resumed through this endpoint.

## Next engineering changes

1. Introduce authenticated reviewer approval, independent from the request originator.
2. Associate each approval with workflow ID, stage, revision, artifact hashes, reviewer, rationale, and timestamp. Invalidate stale approvals.
3. Persist workflow checkpoints and append-only audit events in a durable store.
4. Resume the exact paused graph, rather than restarting with an approval boolean.
5. Run engineering agents in isolated workspaces with allowlisted tools, timeout, resource limits, and patch review.
6. Add failure-injection tests for retry, rollback, safe-stop, and dynamic replanning.
7. Require passing CI and human review before merge or release.

## Verification

Run `mvn -B verify` with Java 21. CI attempts the same build on pull requests. Passing execution was not independently verified when this change was authored.

## Threat model

- Untrusted API clients submit requirements and may claim approval.
- Generated code and natural-language inputs can contain malicious instructions.
- In-memory audit records disappear on restart.
- Replanning can invalidate previously approved change scope.

Not production-ready until these controls are addressed.
