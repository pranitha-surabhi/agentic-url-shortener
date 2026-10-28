package com.example.agentic.workflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SafeStopGovernanceTest {
    @Autowired WorkflowEngine engine;

    @Test
    void stoppedWorkflowCannotBeApprovedOrResumed() {
        WorkflowResult initial = engine.execute(
                "Modify URL aliases", WorkflowScenario.BROWNFIELD);
        assertEquals(WorkflowStatus.WAITING_FOR_APPROVAL, initial.getStatus());
        WorkflowResult stopped = engine.requestSafeStop(
                initial.getWorkflowId(), "Fail-safe operator stop");
        assertEquals(WorkflowStatus.STOPPED, stopped.getStatus());
        assertTrue(stopped.getAuditEvents().stream().anyMatch(
                e -> e.contains("SAFE_STOP requested")));
        assertThrows(IllegalStateException.class, () -> engine.approveAndResume(
                initial.getWorkflowId(), WorkflowState.IMPLEMENTATION, "reviewerOne"));
    }
}
