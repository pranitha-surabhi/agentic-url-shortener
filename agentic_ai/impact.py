"""Static brownfield dependency and API impact inventory (bounded prototype)."""
from pathlib import Path
import re

def inventory(root: Path, target_paths: set[str], max_bytes=40000) -> dict:
    roots = (root / "src/main/java", root / "src/test/java")
    java_files = []
    for directory in roots:
        if directory.exists():
            java_files.extend(sorted(directory.rglob("*.java")))
    if len(java_files) > 2000:
        raise ValueError("Codebase exceeds bounded analysis limit")
    impacted = {}
    sources = {}
    for file in java_files:
        if file.is_symlink() or file.stat().st_size > max_bytes:
            continue
        relative = file.relative_to(root).as_posix()
        text = file.read_text(encoding="utf-8")
        sources[relative] = text
    for target in sorted(target_paths):
        file = root / target
        if target not in sources:
            continue
        text = sources[target]
        declared = re.findall(r"\b(?:class|interface|enum|record)\s+(\w+)", text)
        methods = re.findall(r"\bpublic\s+(?:static\s+)?[\w<>?,.\[\]]+\s+(\w+)\s*\(", text)
        references = []
        for path, other in sources.items():
            if path == target:
                continue
            if any(re.search(r"\b" + re.escape(name) + r"\b", other) for name in declared):
                references.append(path)
        impacted[target] = {"declaredTypes": declared, "publicMethods": methods,
                            "referencedBy": sorted(references),
                            "testReferences": sorted(p for p in references if "/test/" in p),
                            "analysisLimitations": "Name/reference scan, not full semantic or data-flow analysis"}
    return impacted
