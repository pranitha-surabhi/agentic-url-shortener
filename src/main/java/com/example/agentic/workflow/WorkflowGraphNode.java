package com.example.agentic.workflow;

import java.util.ArrayList;
import java.util.List;

public class WorkflowGraphNode {

    private final WorkflowNode workflowNode;

    private final List<WorkflowState> dependencies =
            new ArrayList<>();

    public WorkflowGraphNode(WorkflowNode workflowNode) {
        this.workflowNode = workflowNode;
    }

    public WorkflowNode getWorkflowNode() {
        return workflowNode;
    }

    public List<WorkflowState> getDependencies() {
        return List.copyOf(dependencies);
    }

    public void addDependency(WorkflowState dependency) {
        dependencies.add(dependency);
    }
}