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
        // The current testing node only emits narrative claims: it does not
        // execute a test runner. Do not mistake those claims for test evidence.
        Object testEvidence = context.getStageOutputs().get("verifiedTestEvidence");
        Object implementationEvidence = context.getStageOutputs().get("verifiedImplementationEvidence");
        if (!(testEvidence instanceof Boolean && (Boolean) testEvidence)
                || !(implementationEvidence instanceof Boolean && (Boolean) implementationEvidence)) {
            context.addRisk("Release blocked: verifiable code and test execution evidence is missing");
            context.addDecision("Require trusted CI and implementation evidence before release");
            context.addStageOutput("releaseReadiness", "BLOCKED");
            return;
        }
        context.addValidationResult("Release readiness gate passed with verified execution evidence");
        context.addDecision("Workflow is ready for human release review");
        context.addStageOutput("releaseReadiness", "READY_FOR_HUMAN_REVIEW");
    }
}
