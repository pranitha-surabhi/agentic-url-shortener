import unittest
from agentic_ai.provenance import decision_chain, verify_chain

class ProvenanceTests(unittest.TestCase):
    def test_chain_detects_unauthorized_edits(self):
        chain = decision_chain([
            {"stage": "requirements", "value": "normalize"},
            {"stage": "testing", "result": "passed"},
        ])
        self.assertTrue(verify_chain(chain))
        chain[0]["event"]["value"] = "changed"
        self.assertFalse(verify_chain(chain))

if __name__ == "__main__":
    unittest.main()
