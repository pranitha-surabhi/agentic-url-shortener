"""Manually invoked live LLM check. No test code is executed by this Python process.

A real model proposes a patch. The patch is approved only for a constrained
verification exercise in an ephemeral workspace. Docker executes Maven offline.
"""
from __future__ import annotations
import argparse
import hashlib
import json
import os
import subprocess
import sys
import uuid
from pathlib import Path

from langgraph.checkpoint.sqlite import SqliteSaver
from agentic_ai.engine import build_graph, start, resume

REQUEST = ("Update existing URL validator so URL credentials are rejected. "
           "Add an automated regression test for a URL containing userinfo. "
           "Keep the current URL API backward compatible.")

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo", default=".")
    parser.add_argument("--sandbox", default=".agentic-workspaces")
    parser.add_argument("--report", default="agentic-ai-verification.json")
    args = parser.parse_args()
    if not os.environ.get("OPENAI_API_KEY"):
        print("NOT VERIFIED: OPENAI_API_KEY is not configured.", file=sys.stderr)
        return 2
    repo = Path(args.repo).resolve()
    workspace_root = Path(args.sandbox).resolve()
    workspace_root.mkdir(parents=True, exist_ok=True)
    thread = str(uuid.uuid4())
    db = workspace_root / (thread + ".sqlite")
    # The patch approval in this exercise does not grant production permission.
    # It writes only to an isolated workspace; no GitHub push or deployment.
    with SqliteSaver.from_conn_string(str(db)) as checkpointer:
        graph = build_graph(repo, workspace_root, checkpointer)
        state = start(graph, thread, REQUEST)
        for _ in range(3):
            interruptions = state.get("__interrupt__", [])
            if not interruptions:
                break
            kind = interruptions[0].value["kind"]
            if kind == "clarification":
                state = resume(graph, thread, {"answer":
                    "Reject username/password URL components. Test HTTP(S) behavior using JUnit."})
            elif kind == "patch_approval":
                diff = interruptions[0].value["diff"]
                if not diff.strip() or len(diff) > 100000:
                    raise RuntimeError("No reviewable model patch was produced")
                state = resume(graph, thread, {"approved": True})
            else:
                raise RuntimeError("Unexpected graph interrupt")
        result = state.get("result", {})
        if result.get("status") != "PENDING_SANDBOX_TESTS":
            raise RuntimeError("Expected a generated patch awaiting external sandbox tests")
        target = Path(result["workspace"]).resolve()
        if not target.is_relative_to(workspace_root):
            raise RuntimeError("Workspace escaped sandbox root")
        # No model-controlled paths reach this docker invocation.
        image = "maven:3.9.11-eclipse-temurin-21"
        cache = (Path.home() / ".m2" / "repository").resolve()\n        if not cache.is_dir():\n            raise RuntimeError("Prewarmed offline Maven cache is required")\n        command = ["docker", "run", "--rm", "--network", "none",
            "--user", f"{os.getuid()}:{os.getgid()}", "--cap-drop", "ALL",
            "--security-opt", "no-new-privileges", "--pids-limit", "256",
            "--memory", "1536m", "--cpus", "2", "--read-only",
            "--tmpfs", "/tmp:rw,noexec,nosuid,size=128m",
            "--mount", f"type=bind,source={target},target=/workspace",\n            "--mount", f"type=bind,source={cache},target=/maven-repo,readonly",
            "--workdir", "/workspace",
            "--env", "HOME=/tmp",
            "--env", "MAVEN_OPTS=-Dmaven.repo.local=/maven-repo",
            image, "mvn", "-B", "--offline", "--no-transfer-progress", "verify"]
        # Command runs in an unprivileged container with no API secret provided.
        try:
            p = subprocess.run(command, capture_output=True, text=True, timeout=240)
            exit_code = p.returncode
            log_tail = (p.stdout + "\n" + p.stderr)[-16000:]
        except subprocess.TimeoutExpired:
            exit_code = 124
            log_tail = "Sandboxed Maven validation timed out"
        report = {
            "liveModelCalled": True,
            "patchGenerated": bool(state.get("diff")),
            "isolatedWorkspace": str(target),
            "sourceDiffSha256": hashlib.sha256(state["diff"].encode()).hexdigest(),
            "sandbox": {"network": "none", "privileged": False,
                        "readOnlyRoot": True, "mavenOffline": True},
            "mavenExitCode": exit_code,
            "mavenPassed": exit_code == 0,
            "logTail": log_tail,
        }
        Path(args.report).write_text(json.dumps(report, indent=2))
        print(f"Live LLM patch created; sandbox Maven exit code = {exit_code}")
        return 0 if exit_code == 0 else 1

if __name__ == "__main__":
    raise SystemExit(main())
