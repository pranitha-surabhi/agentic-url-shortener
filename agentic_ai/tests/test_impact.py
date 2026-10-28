import tempfile
import unittest
from pathlib import Path
from agentic_ai.impact import inventory

class BrownfieldImpactTests(unittest.TestCase):
    def test_detects_public_api_callers_and_test_dependencies(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            target = "src/main/java/com/example/UrlValidator.java"
            validator = root / target
            caller = root / "src/main/java/com/example/UrlService.java"
            test = root / "src/test/java/com/example/UrlValidatorTest.java"
            for file in (validator, caller, test):
                file.parent.mkdir(parents=True, exist_ok=True)
            validator.write_text("public class UrlValidator { public boolean check(String url) { return true; } }")
            caller.write_text("class UrlService { UrlValidator validator; }")
            test.write_text("class UrlValidatorTest { UrlValidator subject; }")
            report = inventory(root, {target})[target]
            self.assertEqual(["UrlValidator"], report["declaredTypes"])
            self.assertIn("check", report["publicMethods"])
            self.assertIn(caller.relative_to(root).as_posix(), report["referencedBy"])
            self.assertIn(test.relative_to(root).as_posix(), report["testReferences"])

if __name__ == "__main__":
    unittest.main()
