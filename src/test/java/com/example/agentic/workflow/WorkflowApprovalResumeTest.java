package com.example.agentic.workflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WorkflowApprovalResumeTest {
    @Autowired WorkflowEngine engine;

    @Test
    void brownfieldRequiresSeparateApprovalsForCodeAndRelease() {
        WorkflowResult initial = engine.execute(
                "modify existing URL shortener to add alias",
                WorkflowScenario.BROWNFIELD, false);
        assertEquals(WorkflowStatus.WAITING_FOR_APPROVAL, initial.getStatus());
        assertFalse(initial.isApprovalGranted());

        WorkflowResult implementationApproved = engine.approveAndResume(
                initial.getWorkflowId(), WorkflowState.IMPLEMENTATION, "reviewer1");
        assertEquals(WorkflowStatus.WAITING_FOR_APPROVAL, implementationApproved.getStatus());

        WorkflowResult releaseApproved = engine.approveAndResume(
                initial.getWorkflowId(), WorkflowState.RELEASE_READINESS, "reviewer1");
        assertEquals(WorkflowStatus.COMPLETED, releaseApproved.getStatus());
        assertEquals("READY_FOR_HUMAN_REVIEW",
                releaseApproved.getStageOutputs().get("releaseReadiness"));
    }
}
