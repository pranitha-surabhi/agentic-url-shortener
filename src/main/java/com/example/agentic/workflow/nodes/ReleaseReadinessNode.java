package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.WorkflowContext;
import com.example.agentic.workflow.WorkflowNode;
import com.example.agentic.workflow.WorkflowState;
import org.springframework.stereotype.Component;

@Component
public class ReleaseReadinessNode implements WorkflowNode {

    @Override
    public String getName() {
        return "Release Readiness";
    }

    @Override
    public WorkflowState getState() {
        return WorkflowState.RELEASE_READINESS;
    }

    @Override
    public void execute(WorkflowContext context) {

        if (context.getValidationResults().isEmpty()) {
            throw new IllegalStateException(
                    "Release cannot proceed without validation"
            );
        }

        context.addValidationResult(
                "Release readiness gate passed"
        );

        context.addDecision(
                "Workflow is ready for completion"
        );

        context.addStageOutput(
                "releaseReadiness",
                "READY"
        );
    }
}