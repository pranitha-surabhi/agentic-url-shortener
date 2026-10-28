"""Minimal hash-chained decision provenance for demonstration evidence."""
from __future__ import annotations
import hashlib
import json

def fingerprint(data: str) -> str:
    return hashlib.sha256(data.encode("utf-8")).hexdigest()

def decision_chain(events: list[dict]) -> list[dict]:
    chain = []
    previous = "GENESIS"
    for event in events:
        content = {"event": event, "previous": previous}
        signature = fingerprint(json.dumps(content, sort_keys=True))
        chain.append({**content, "sha256": signature})
        previous = signature
    return chain

def verify_chain(chain: list[dict]) -> bool:
    previous = "GENESIS"
    for entry in chain:
        content = {"event": entry["event"], "previous": previous}
        if entry["previous"] != previous or entry["sha256"] != fingerprint(
                json.dumps(content, sort_keys=True)):
            return False
        previous = entry["sha256"]
    return True
