import hashlib
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from langgraph.checkpoint.memory import InMemorySaver
from agentic_ai.provenance import verify_chain
from agentic_ai.engine import (
    ALLOWLIST, ProposedPatch, ClarificationDecision, build_graph, start, resume, _clean_path
)


class FakeModel:
    def __init__(self, ambiguous=False):
        self.ambiguous = ambiguous

    def with_structured_output(self, schema):
        parent = self

        class Runnable:
            def invoke(self, prompt):
                if schema is ClarificationDecision:
                    return ClarificationDecision(
                        needs_clarification=parent.ambiguous,
                        question="Define success criteria",
                        acceptance_criteria=["Tests must pass"])
                if schema is ProposedPatch:
                    path = next(iter(sorted(ALLOWLIST)))
                    return ProposedPatch(
                        rationale="A real test modification",
                        changes=[{"path": path, "content": "public class Changed {}\n"}],
                        tests=["mvn verify"], risk="Requires CI review")
                raise AssertionError(schema)
        return Runnable()


class GraphScenarios(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.repo = self.root / "repo"
        self.repo.mkdir()
        for name in ALLOWLIST:
            path = self.repo / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text("class Original {}\n", encoding="utf-8")
        self.sandbox = self.root / "sandbox"

    def test_greenfield_approval_changes_only_workspace(self):
        with patch("agentic_ai.engine._model", return_value=FakeModel()):
            graph = build_graph(self.repo, self.sandbox, InMemorySaver())
            paused = start(graph, "greenfield", "Create a URL feature")
            self.assertIn("__interrupt__", paused)
            approved = resume(graph, "greenfield", {"approved": True, "patchSha256": hashlib.sha256(paused["__interrupt__"][0].value["diff"].encode()).hexdigest()})
            self.assertEqual("PENDING_SANDBOX_TESTS", approved["result"]["status"])
            self.assertEqual("APPROVED", approved["summary"]["reviewDecision"])
            self.assertEqual("PENDING_SANDBOX_TESTS", approved["summary"]["validationStatus"])
            self.assertTrue(approved["summary"]["patchSha256"])
            self.assertTrue(verify_chain(approved["lineage"]))
            workspace = Path(approved["workspace"])
            changed = sorted(ALLOWLIST)[0]
            self.assertIn("Changed", (workspace / changed).read_text())
            self.assertIn("Original", (self.repo / changed).read_text())

    def test_brownfield_rejected_never_applies(self):
        with patch("agentic_ai.engine._model", return_value=FakeModel()):
            graph = build_graph(self.repo, self.sandbox, InMemorySaver())
            paused = start(graph, "brownfield", "Update existing URL validator")
            self.assertIn("__interrupt__", paused)
            rejected = resume(graph, "brownfield", {"approved": False})
            self.assertEqual("REJECTED", rejected["result"]["status"])
            self.assertEqual("SAFE_STOP", rejected["summary"]["fallback"]["strategy"])
            self.assertNotIn("workspace", rejected)

    def test_ambiguous_requirement_requests_clarification_before_patch(self):
        with patch("agentic_ai.engine._model", return_value=FakeModel(ambiguous=True)):
            graph = build_graph(self.repo, self.sandbox, InMemorySaver())
            first = start(graph, "ambiguous", "Make it faster")
            self.assertEqual("clarification", first["__interrupt__"][0].value["kind"])
            second = resume(graph, "ambiguous", {"answer": "P95 latency under 100ms"})
            self.assertEqual("patch_approval", second["__interrupt__"][0].value["kind"])
            final = resume(graph, "ambiguous", {"approved": False})
            self.assertEqual("REJECTED", final["result"]["status"])

    def test_approval_with_wrong_patch_hash_cannot_apply(self):
        with patch("agentic_ai.engine._model", return_value=FakeModel()):
            graph = build_graph(self.repo, self.sandbox, InMemorySaver())
            paused = start(graph, "wrong-hash", "Update existing validator")
            self.assertIn("__interrupt__", paused)
            result = resume(graph, "wrong-hash", {"approved": True,
                                                 "patchSha256": "incorrect"})
            self.assertEqual("REJECTED", result["result"]["status"])
            self.assertTrue(result["result"]["safeStop"])
            self.assertEqual("SAFE_STOP", result["fallback"]["strategy"])
            self.assertFalse(self.sandbox.joinpath("agentic-workspaces").exists())

    def test_path_escape_is_rejected(self):
        with self.assertRaises(ValueError):
            _clean_path("../../.env")


if __name__ == "__main__":
    unittest.main()
