package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.WorkflowContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseReadinessNodeTest {
    @Test
    void blocksReleaseWithoutVerifiedExecution() {
        WorkflowContext context = new WorkflowContext();
        context.addValidationResult("Unit tests executed"); // narrative is not evidence
        new ReleaseReadinessNode().execute(context);
        assertEquals("BLOCKED", context.getStageOutputs().get("releaseReadiness"));
        assertTrue(context.getRisks().stream().anyMatch(r -> r.contains("Release blocked")));
    }

    @Test
    void permitsReviewOnlyWithIndependentEvidenceFlags() {
        WorkflowContext context = new WorkflowContext();
        context.addStageOutput("verifiedTestEvidence", true);
        context.addStageOutput("verifiedImplementationEvidence", true);
        new ReleaseReadinessNode().execute(context);
        assertEquals("READY_FOR_HUMAN_REVIEW", context.getStageOutputs().get("releaseReadiness"));
    }
}
