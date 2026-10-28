package com.example.agentic.workflow;

public record WorkflowDependency(
        WorkflowState from,
        WorkflowState to
) {
}