package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.WorkflowContext;
import com.example.agentic.workflow.WorkflowNode;
import com.example.agentic.workflow.WorkflowState;
import org.springframework.stereotype.Component;

@Component
public class ImplementationNode implements WorkflowNode {

    @Override
    public String getName() {
        return "Implementation";
    }

    @Override
    public WorkflowState getState() {
        return WorkflowState.IMPLEMENTATION;
    }

    @Override
    public void execute(WorkflowContext context) {

        if (context.getIdentifiedTasks().isEmpty()) {
            throw new IllegalStateException(
                    "Implementation cannot start without decomposed tasks"
            );
        }

        context.addDecision(
                "Implementation proceeds using the approved architecture"
        );

        context.addArtifact(
                "URL shortening REST API"
        );

        context.addArtifact(
                "In-memory repository implementation"
        );

        context.addArtifact(
                "Redirect and analytics functionality"
        );

        context.addArtifact(
                "Global exception handling"
        );

        context.addStageOutput(
                "implementation",
                "Implementation completed according to planned architecture"
        );
    }
}