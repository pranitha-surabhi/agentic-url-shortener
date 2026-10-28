package com.example.agentic.workflow;

public interface WorkflowNode {

    String getName();

    WorkflowState getState();

    void execute(WorkflowContext context);

}