"""Review-gated LangGraph SDLC agent. No untrusted code is executed on the host.

Usage: initialize with repo root and sandbox root, start(thread, request), resume(thread, response).
A separate, persisted SQLite checkpointer is required for resumable interrupts.
"""
from __future__ import annotations

import difflib
import hashlib
import json
import os
import shutil
import subprocess
import tempfile
from pathlib import Path
from typing import TypedDict

from langgraph.graph import StateGraph, START, END
from langgraph.types import interrupt, Command
from langchain_openai import ChatOpenAI
from pydantic import BaseModel, Field

# Only a small set of business files can be changed by an agent.
ALLOWLIST = {
    "src/main/java/com/example/agentic/used/validator/UrlValidator.java",
    "src/main/java/com/example/agentic/service/UrlShortenerService.java",
    "src/test/java/com/example/agentic/service/UrlShortenerServiceTest.java",
}
MAX_FILE = 40000

class Change(BaseModel):
    path: str = Field(description="One of the exact allowlisted repository paths")
    content: str = Field(max_length=MAX_FILE, description="Complete replacement source file")

class ProposedPatch(BaseModel):
    rationale: str
    changes: list[Change] = Field(min_length=1, max_length=3)
    tests: list[str]
    risk: str

class ClarificationDecision(BaseModel):
    needs_clarification: bool
    question: str
    acceptance_criteria: list[str] = Field(min_length=1)

class State(TypedDict, total=False):
    request: str
    clarified: str
    criteria: list[str]
    proposal: dict
    diff: str
    approved: bool
    workspace: str
    result: dict
    evidence: list[str]

def _model():
    if not os.environ.get("OPENAI_API_KEY"):
        raise RuntimeError("OPENAI_API_KEY is required for actual LangChain model execution")
    return ChatOpenAI(model=os.getenv("AGENTIC_MODEL", "gpt-4.1"), temperature=0)

def _clean_path(path: str) -> str:
    if path not in ALLOWLIST or Path(path).is_absolute() or ".." in Path(path).parts:
        raise ValueError("Agent requested path outside allowlist")
    return path

def _copy_repo(source: Path, destination: Path) -> None:
    if destination.exists():
        raise FileExistsError(destination)
    shutil.copytree(source, destination, ignore=shutil.ignore_patterns(
        ".git", "target", ".venv", "data", "__pycache__", ".env", "*.db"))
    for base, directories, files in os.walk(destination):
        for name in directories + files:
            path = Path(base) / name
            if path.is_symlink():
                raise ValueError("Symlink in workspace is not permitted")

def build_graph(repo: Path, sandbox: Path, checkpointer):
    repo = repo.resolve(strict=True)
    sandbox = sandbox.resolve()
    sandbox.mkdir(parents=True, exist_ok=True)

    def clarify(state: State):
        decision = _model().with_structured_output(ClarificationDecision).invoke(
            "You are a requirements analyst. Determine if the request needs a specific "
            "acceptance criterion clarified. Do not invent requirements. Request: "
            + state["request"])
        request = state["request"]
        if decision.needs_clarification:
            response = interrupt({"kind": "clarification",
                                  "question": decision.question,
                                  "initialCriteria": decision.acceptance_criteria})
            if not isinstance(response, dict) or not str(response.get("answer", "")).strip():
                raise ValueError("Human clarification answer is required")
            request += "\nHuman clarification: " + response["answer"].strip()
        return {"clarified": request, "criteria": decision.acceptance_criteria}

    def propose(state: State):
        originals = {}
        for path in sorted(ALLOWLIST):
            file = repo / path
            if file.is_file():
                content = file.read_text(encoding="utf-8")
                if len(content) > MAX_FILE:
                    raise ValueError("File too large for model change")
                originals[path] = content
        prompt = {
            "task": state["clarified"],
            "acceptance_criteria": state["criteria"],
            "repository_files": originals,
            "constraints": [
                "Make minimal real changes to the existing URL shortener",
                "Only return complete files from the supplied allowlist",
                "Preserve public method signatures unless needed",
                "Add or update JUnit tests for changed behavior",
                "No shell commands, deployment, secret access or network code",
            ],
        }
        proposal = _model().with_structured_output(ProposedPatch).invoke(
            "Act as a Java 21 Spring Boot engineering agent. Produce a safe source patch, "
            "not a prose implementation claim. JSON context:\n" + json.dumps(prompt))
        seen = set()
        diffs = []
        for change in proposal.changes:
            path = _clean_path(change.path)
            if path in seen or path not in originals:
                raise ValueError("Duplicate or nonexistent patch target")
            seen.add(path)
            old = originals[path]
            if not change.content.strip() or len(change.content) > MAX_FILE:
                raise ValueError("Unsafe or oversized patch")
            diffs.extend(difflib.unified_diff(old.splitlines(True),
                         change.content.splitlines(True), fromfile="a/"+path,
                         tofile="b/"+path))
        if not diffs:
            raise ValueError("No source changes were generated")
        return {"proposal": proposal.model_dump(),
                "diff": "".join(diffs),
                "evidence": ["Generated a change-specific diff for review"]}

    def review(state: State):
        decision = interrupt({"kind": "patch_approval",
                 "diff": state["diff"],
                 "risk": state["proposal"]["risk"],
                 "tests": state["proposal"]["tests"],
                 "instruction": "Only an authorized reviewer should approve this patch."})
        approved = isinstance(decision, dict) and decision.get("approved") is True
        return {"approved": approved}

    def route_review(state: State):
        return "apply" if state.get("approved") else "reject"

    def reject(state: State):
        return {"result": {"status": "REJECTED", "applied": False}}

    def apply(state: State):
        # Everything runs in an isolated copy; the original Git checkout is untouched.
        workspace = Path(tempfile.mkdtemp(prefix="agentic-", dir=sandbox))
        shutil.rmtree(workspace)
        try:
            _copy_repo(repo, workspace)
            hashes = {}
            for change in state["proposal"]["changes"]:
                relative = _clean_path(change["path"])
                target = workspace / relative
                target.write_text(change["content"], encoding="utf-8")
                hashes[relative] = hashlib.sha256(
                    change["content"].encode("utf-8")).hexdigest()
            return {"workspace": str(workspace), "evidence":
                    state.get("evidence", []) + [json.dumps(hashes, sort_keys=True)]}
        except Exception:
            shutil.rmtree(workspace, ignore_errors=True)
            raise

    def validate(state: State):
        workspace = Path(state["workspace"])
        # IMPORTANT: Maven executes build plugins and code from the repository;
        # use an OS/container sandbox with no secrets or network and unprivileged user.
        # This runner is intentionally opt-in and disabled by default.
        if os.getenv("AGENTIC_ENABLE_TEST_EXECUTION") != "1":
            return {"result": {"status": "PENDING_SANDBOX_TESTS",
                     "workspace": str(workspace),
                     "applied": True, "verified": False}}
        try:
            run = subprocess.run(["mvn", "-B", "--offline", "verify"],
                cwd=workspace, timeout=180, capture_output=True, text=True,
                env={"PATH": os.environ.get("PATH", ""),
                     "HOME": str(workspace), "JAVA_HOME": os.environ.get("JAVA_HOME", "")})
            output = (run.stdout + "\n" + run.stderr)[-12000:]
            return {"result": {"status": "TESTS_PASSED" if run.returncode == 0
                    else "TESTS_FAILED", "verified": run.returncode == 0,
                    "applied": True, "workspace": str(workspace),
                    "exitCode": run.returncode, "logTail": output}}
        except subprocess.TimeoutExpired:
            return {"result": {"status": "TEST_TIMEOUT", "verified": False,
                    "applied": True, "workspace": str(workspace)}}

    builder = StateGraph(State)
    for name, fn in [("clarify", clarify), ("propose", propose),
                     ("review", review), ("apply", apply),
                     ("validate", validate), ("reject", reject)]:
        builder.add_node(name, fn)
    builder.add_edge(START, "clarify")
    builder.add_edge("clarify", "propose")
    builder.add_edge("propose", "review")
    builder.add_conditional_edges("review", route_review,
                                   {"apply": "apply", "reject": "reject"})
    builder.add_edge("apply", "validate")
    builder.add_edge("validate", END)
    builder.add_edge("reject", END)
    return builder.compile(checkpointer=checkpointer)

def start(graph, thread: str, requirement: str):
    if not requirement.strip() or len(requirement) > 4000:
        raise ValueError("Requirement must have 1..4000 characters")
    return graph.invoke({"request": requirement},
          config={"configurable": {"thread_id": thread}})

def resume(graph, thread: str, human_response: dict):
    return graph.invoke(Command(resume=human_response),
          config={"configurable": {"thread_id": thread}})
