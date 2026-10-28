package com.example.agentic.workflow;

public class ApprovalGate {

    public boolean requiresApproval(
            WorkflowContext context,
            WorkflowState state) {

        if (!context.isApprovalRequired()) {
            return false;
        }

        return state == WorkflowState.IMPLEMENTATION
                || state == WorkflowState.RELEASE_READINESS;
    }

    public boolean isApproved(
            WorkflowContext context) {

        return context.isApprovalGranted();
    }
}