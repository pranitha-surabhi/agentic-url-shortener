# Agentic Automation, LangGraph, LangChain, LLM and MCP — Implementation Guide

This document maps technology claims to **actual code on `main`**. The Java Spring Boot workflow and the optional Python LangGraph agent are distinct cooperating implementations, not a single automatically integrated deployment.

## Technology coverage

| Concept | Present in source? | Evidence | Verification boundary |
|---|---|---|---|
| Agentic software engineering automation | Yes | `src/main/java/com/example/agentic/workflow/WorkflowEngine.java`, `agentic_ai/engine.py` | Workflow and mock-model automated tests |
| LangGraph | Yes | `StateGraph`, conditional review transitions, `interrupt`, resume in `agentic_ai/engine.py` | Python CI with mock model |
| LangChain integration | Yes | `langchain_openai.ChatOpenAI` and `with_structured_output` | Source and mock-model tests; live API run still requires credentials |
| LLM-backed analysis / code generation | Implemented, live run not verified | `clarify` and `propose` nodes call `ChatOpenAI` | Requires `OPENAI_API_KEY` and successful live workflow |
| Human-in-the-loop | Yes for prototype | Clarification and patch approval interrupts, patch SHA-256 binding | Authorization relies on the CLI operator; not an enterprise identity system |
| State persistence | Yes for prototype | LangGraph SQLite checkpoint; Java workflow snapshot store | Local durable state, not distributed coordination |
| Java source change validation | Conditional | Isolated workspace and manual Docker Maven verification workflow | The live model + Docker validation must pass |
| MCP (Model Context Protocol) | **Not implemented** | No MCP server/client or tool schema in the checked repository | Potential future enhancement, not an assignment prerequisite |
| MCO | **Not identified** | No implementation identified under this name | Clarify term before claiming support |

## Engineering lifecycle and orchestration

```text
Engineering requirement
    |
    +--> Java SDLC graph: requirement -> decomposition -> architecture
    |                           |                         |
    |                           +-> implementation       +-> risk review
    |                                  |                       |
    |                                  +-> tests + docs -------+
    |                                              |
    |                                       release readiness
    |                         approvals / retries / snapshots / metrics
    |
    +--> Opt-in LangGraph LLM agent:
             clarify (LLM; interrupt if ambiguous)
                |
             propose (LLM; read existing allowlisted Java files, impact inventory)
                |
             review (human interrupt; diff SHA-256 must match)
                |                        |
             approved                 rejected
                |                        |
          apply to sandbox       fail-closed safe stop
                |                        |
             validate           final engineering summary
                |
          final engineering summary and decision provenance
```

The Python agent is started separately from the Java REST application. It does **not** automatically deploy to production or automatically merge generated code into GitHub.

## How LangGraph is used

`agentic_ai/engine.py` builds a `StateGraph` with nodes for requirement clarification, LLM patch proposal, human review, sandbox application, validation, safe rejection and summary. `interrupt` pauses at human decision boundaries. Resume uses the same thread ID and a SQLite checkpoint. Conditional edges prevent a rejected patch from being applied.

## How LangChain and LLMs are used

`langchain_openai.ChatOpenAI` provides structured LLM calls:
- `ClarificationDecision` identifies missing acceptance criteria and asks for a human answer when needed.
- `ProposedPatch` produces source-file replacements, an engineering rationale, tests and risk descriptions.
- A static inventory in `agentic_ai/impact.py` provides Java type/method/caller references as bounded brownfield context.
- `agentic_ai/provenance.py` provides hash-chained decision records.

A model call is **not verified live** merely because mock-model tests pass. The manual live workflow must execute successfully to prove actual model-backed generation and sandbox Maven validation.

## Human patch approval — corrected example

When the CLI returns a `patch_approval` interrupt, inspect its `diff` and copy the **exact** `patchSha256` value supplied by that interrupt. Do not invent a hash and do not approve a diff you have not reviewed.

```bash
python -m agentic_ai.cli start --thread demo-brownfield --requirement "Reject URLs containing userinfo and add a regression test"
# Inspect the patch_approval interrupt output and its patchSha256.
python -m agentic_ai.cli resume --thread demo-brownfield --response '{"approved":true,"patchSha256":"<exact-patchSha256-from-interrupt>"}'
```

The JSON value above is a placeholder: substitute the actual digest from the reviewed interrupt. A bare `{"approved":true}` is **not sufficient**. Reject with `{"approved":false}`. This binding reduces approval-to-patch mismatch but is **not a substitute for reviewer authentication**.

## MCP integration status and possible future design

**MCP is not implemented** in this repository today. It is not explicitly required by the interview assignment. A future MCP gateway could expose restricted, typed tools for:
1. Read-only repository search and dependency inventory.
2. Read-only workflow status, audit evidence and test reports.
3. Review-gated sandbox validation, without model-provided arbitrary shell commands.

Such a gateway must enforce identity, authorization, tool allowlists, timeouts, audit events and isolation. Do not treat this proposed interface as an existing feature. The term **MCO** is not recognized as a component in this repository; if intended to mean MCP, use the description above.

## Demonstration, evidence and limitations

- **Java CI:** `.github/workflows/verify.yml` runs Maven checks for the Java application.
- **Python CI:** `.github/workflows/agentic-python.yml` exercises LangGraph behavior with test doubles (no live key).
- **Live model:** `.github/workflows/agentic-live-verification.yml` is manually triggered and requires a secret named `OPENAI_API_KEY` in the `agentic-live-verification` GitHub environment.
- **Last investigated live attempt:** Run `40770220347` failed before model invocation because the environment variable was empty; it is **not evidence of a successful live run**.
- **Sandbox:** External Docker verification is opt-in; `AGENTIC_ENABLE_TEST_EXECUTION=1` in the generic local graph alone does not create an OS-security boundary.
- **Approval limits:** The Python CLI has no built-in authenticated reviewer identity, and audit hashes are tamper-evidence for a local prototype, not externally anchored enterprise audit logs.
- **Brownfield limit:** Impact inventory scans Java declarations and references; it is not compiler-level program analysis.
- **Autonomy limit:** Generated changes remain in an isolated workspace. There is no automatic production deployment.

For the assignment's exact 25-point acceptance criteria, see `docs/DEMO-ACCEPTANCE.md`. Only mark a requirement fully satisfied when its associated evidence has been observed.
