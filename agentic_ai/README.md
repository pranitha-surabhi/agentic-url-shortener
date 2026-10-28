# LangGraph + LangChain AI software engineering agent

This opt-in Python service works alongside the existing Spring Boot URL shortener. It uses an actual LangChain ChatOpenAI model for requirement interpretation and Java patch generation, and LangGraph for **clarification interrupts**, **human patch approval**, and **resumption on the same persisted thread**. It generates real replacement code and unified diffs for existing project files.

## Setup

Run from repository root with Python 3.11+:

```bash
python -m venv .venv
. .venv/bin/activate
pip install -r agentic_ai/requirements.txt
export OPENAI_API_KEY=... # use your own environment or secret manager, never commit secrets
python -m agentic_ai.cli start --thread demo-greenfield --requirement "Build stricter URL validation"
```

The graph may return `__interrupt__` asking for missing acceptance criteria or a patch review. To answer:

```bash
python -m agentic_ai.cli resume --thread demo-greenfield --response '{"answer":"Only HTTP(S), reject credentials in URLs"}'
python -m agentic_ai.cli resume --thread demo-greenfield --response '{"approved":true}'
```

Use separate thread IDs for greenfield, brownfield, and ambiguous demos. Brownfield request example: "Update the existing URL validator to reject URLs with userinfo and add a regression test." Ambiguous example: "Make URL redirects faster" (answer the latency target, supported load and measurement procedure when interrupted).

## Isolation and security

- A model does not directly modify the checked-out repository. Proposed replacements are limited to three **exact allowlisted Java paths**.
- The system requires a human decision before applying changes to an **isolated workspace copy**. Rejecting the proposal leaves source untouched.
- Test execution is **off by default**. Set `AGENTIC_ENABLE_TEST_EXECUTION=1` **only inside a separately provisioned sandboxed container/VM** with network disabled, no secrets, unprivileged filesystem, resource limits and permitted Maven caches; the Python process itself does *not* establish OS-level isolation.
- Runs Maven `--offline verify` in that workspace after approval; records status and bounded log output.
- The SQLite checkpointer supports restarting the CLI process while awaiting clarification or approval; it is a local prototype, not multi-tenant protected storage.
- No change is pushed or merged automatically. Manually inspect the patch and test evidence, then open a separate PR under your normal branch protections.
- API authentication and approver identity verification are **not** provided by this CLI. The person operating it must be authorized, and enterprise use requires OIDC/RBAC, signed artifacts, and a secured runner.

## Scope

This is actual model-backed planning and patch generation, not the previous deterministic template-only executor. It does not guarantee the model's output is correct or that arbitrary new requirements are fully implemented. It does not automate production deployment. LangChain and LangGraph functionality requires installing Python dependencies and a working model API key.
