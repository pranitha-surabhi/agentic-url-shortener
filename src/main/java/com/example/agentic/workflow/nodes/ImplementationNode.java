package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.EngineeringEvidenceService;
import com.example.agentic.workflow.WorkflowContext;
import com.example.agentic.workflow.WorkflowNode;
import com.example.agentic.workflow.WorkflowState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImplementationNode implements WorkflowNode {
    private final EngineeringEvidenceService engineeringExecutor;

    @Override public String getName() { return "Implementation"; }
    @Override public WorkflowState getState() { return WorkflowState.IMPLEMENTATION; }

    @Override
    public void execute(WorkflowContext context) {
        if (context.getIdentifiedTasks().isEmpty()) {
            throw new IllegalStateException("Implementation requires task decomposition");
        }
        EngineeringEvidenceService.Evidence evidence =
                engineeringExecutor.generateAndValidate(context.getWorkflowId(), context.getRequirement());
        if (!evidence.compilationPassed()) {
            throw new IllegalStateException("Generated code did not compile");
        }
        context.addArtifact("Generated source: " + evidence.sourcePath());
        context.addArtifact("SHA-256: " + evidence.sha256());
        context.addStageOutput("engineeringEvidence", evidence);
        context.addStageOutput("implementation", "Generated deterministic URL policy source; SHA-256="
                + evidence.sha256());
        context.addStageOutput("verifiedImplementationEvidence", true);
        context.addDecision("Generated isolated Java source under controlled deterministic templates");
    }
}
