package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.*;
import org.springframework.stereotype.Component;
import java.util.Locale;

@Component
public class TaskDecompositionNode implements WorkflowNode {
    @Override public String getName() { return "Task Decomposition"; }
    @Override public WorkflowState getState() { return WorkflowState.TASK_DECOMPOSITION; }

    @Override
    public void execute(WorkflowContext context) {
        String requirement = context.getRequirement().toLowerCase(Locale.ROOT);
        context.addTask("Define scope and measurable acceptance criteria");
        context.addTask("Review the URL API contract and persistence boundaries");
        if (requirement.contains("analytic") || requirement.contains("click")) {
            context.addTask("Review click events and counters for concurrency safety");
            context.addTask("Add analytics accuracy and load regression tests");
        }
        if (requirement.contains("expire") || requirement.contains("ttl")) {
            context.addTask("Implement expiry policy and expiration edge-case tests");
        }
        if (requirement.contains("alias") || requirement.contains("custom")) {
            context.addTask("Design custom alias uniqueness and conflict handling");
            context.addTask("Add collision and concurrent request tests");
        }
        if (requirement.contains("redirect") || requirement.contains("performance")
                || requirement.contains("faster")) {
            context.addTask("Measure redirect baseline and define latency target");
            context.addTask("Verify redirect path and indexing strategy");
        }
        if (context.getScenario() == WorkflowScenario.BROWNFIELD) {
            context.addTask("Inspect impacted source classes and API dependencies");
            context.addTask("Prepare backward-compatible change and rollback plan");
        }
        if (context.getScenario() == WorkflowScenario.AMBIGUOUS) {
            context.addTask("Request clarification of non-measurable acceptance criteria");
            context.setApprovalRequired(true);
        }
        context.addTask("Generate reviewable code artifact and record hash");
        context.addTask("Compile and execute deterministic acceptance tests");
        context.addTask("Summarize risks, changes and validation evidence");
        context.addStageOutput("taskDecomposition", ListCopy.copy(context.getIdentifiedTasks()));
    }

    private static final class ListCopy {
        private static java.util.List<String> copy(java.util.List<String> input) {
            return java.util.List.copyOf(input);
        }
    }
}
