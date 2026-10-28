package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.EngineeringEvidenceService;
import com.example.agentic.workflow.WorkflowContext;
import com.example.agentic.workflow.WorkflowNode;
import com.example.agentic.workflow.WorkflowState;
import org.springframework.stereotype.Component;

@Component
public class TestingNode implements WorkflowNode {
    @Override public String getName() { return "Testing"; }
    @Override public WorkflowState getState() { return WorkflowState.TESTING; }

    @Override
    public void execute(WorkflowContext context) {
        Object evidenceObject = context.getStageOutputs().get("engineeringEvidence");
        if (!(evidenceObject instanceof EngineeringEvidenceService.Evidence evidence)) {
            throw new IllegalStateException("No actual engineering evidence to test");
        }
        if (!evidence.compilationPassed() || !evidence.assertionsPassed()
                || evidence.testsExecuted() < 3) {
            throw new IllegalStateException("Compiled source or its acceptance tests failed");
        }
        context.addArtifact("Test results: " + evidence.testReportPath());
        context.addValidationResult("Generated source compilation passed");
        context.addValidationResult("Generated source acceptance tests passed: "
                + evidence.testsExecuted());
        context.addStageOutput("verifiedTestEvidence", true);
        context.addStageOutput("testing", evidence.testReportPath());
    }
}
