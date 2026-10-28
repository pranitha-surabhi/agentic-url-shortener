"""Invoke LangGraph agent and resume clarification/approval with the same thread ID."""
import argparse
import json
import os
from pathlib import Path
from langgraph.checkpoint.sqlite import SqliteSaver
from .engine import build_graph, start, resume

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("operation", choices=("start", "resume"))
    parser.add_argument("--thread", required=True)
    parser.add_argument("--requirement")
    parser.add_argument("--response", help='JSON: {"answer":"..."} or {"approved":true}')
    parser.add_argument("--repo", default=".")
    parser.add_argument("--sandbox", default=".agentic-workspaces")
    parser.add_argument("--checkpoint", default=".agentic-checkpoints.sqlite")
    args = parser.parse_args()
    with SqliteSaver.from_conn_string(args.checkpoint) as saver:
        graph = build_graph(Path(args.repo), Path(args.sandbox), saver)
        if args.operation == "start":
            result = start(graph, args.thread, args.requirement or "")
        else:
            result = resume(graph, args.thread, json.loads(args.response or "{}"))
        print(json.dumps(result, default=str, indent=2))

if __name__ == "__main__":
    main()
