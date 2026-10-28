package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.WorkflowContext;
import com.example.agentic.workflow.WorkflowNode;
import com.example.agentic.workflow.WorkflowState;
import org.springframework.stereotype.Component;

@Component
public class DocumentationNode implements WorkflowNode {

    @Override
    public String getName() {
        return "Documentation";
    }

    @Override
    public WorkflowState getState() {
        return WorkflowState.DOCUMENTATION;
    }

    @Override
    public void execute(WorkflowContext context) {

        context.addArtifact(
                "Project README"
        );

        context.addArtifact(
                "Architecture documentation"
        );

        context.addArtifact(
                "API documentation"
        );

        context.addArtifact(
                "Testing and validation documentation"
        );

        context.addStageOutput(
                "documentation",
                "Documentation artifacts identified"
        );
    }
}