package com.example.agentic.workflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WorkflowReplanGovernanceTest {
    @Autowired WorkflowEngine engine;

    @Test
    void replanInvalidatesPriorApprovalsAndRequiresNewHumanReview() {
        WorkflowResult first = engine.execute("Add support for custom URL alias", WorkflowScenario.BROWNFIELD);
        assertEquals(WorkflowStatus.WAITING_FOR_APPROVAL, first.getStatus());
        WorkflowResult next = engine.approveAndResume(
                first.getWorkflowId(), WorkflowState.IMPLEMENTATION, "reviewerOne");
        assertEquals(WorkflowStatus.WAITING_FOR_APPROVAL, next.getStatus());
        WorkflowResult completed = engine.approveAndResume(
                first.getWorkflowId(), WorkflowState.RELEASE_READINESS, "reviewerTwo");
        assertEquals(WorkflowStatus.COMPLETED, completed.getStatus());
        assertTrue(completed.isApprovalGranted());

        WorkflowResult replanned = engine.replan(first.getWorkflowId(),
                WorkflowState.TASK_DECOMPOSITION, "Change plan: require alias collision handling");
        assertEquals(WorkflowStatus.WAITING_FOR_APPROVAL, replanned.getStatus());
        assertFalse(replanned.isApprovalGranted());
        assertFalse(replanned.getStageOutputs().containsKey("testing"));
        assertFalse(replanned.getStageOutputs().containsKey("releaseReadiness"));
        assertTrue(replanned.getAuditEvents().stream().anyMatch(
                e -> e.contains("Invalidated affected downstream outputs")));
    }

    @Test
    void cannotApproveInactiveStage() {
        WorkflowResult first = engine.execute("Add URL alias", WorkflowScenario.BROWNFIELD);
        assertThrows(IllegalArgumentException.class, () -> engine.approveAndResume(
                first.getWorkflowId(), WorkflowState.RELEASE_READINESS, "reviewerOne"));
    }
}
