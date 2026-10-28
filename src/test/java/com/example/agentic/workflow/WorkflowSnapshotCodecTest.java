package com.example.agentic.workflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.UUID;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WorkflowSnapshotCodecTest {
    @TempDir Path dir;

    @Test
    void restoresWorkflowContextAndTypedEvidenceAcrossInstances() {
        String id = UUID.randomUUID().toString();
        WorkflowAuditJournal journal = new WorkflowAuditJournal(dir.resolve("audit"));
        WorkflowExecutionStore first = new WorkflowExecutionStore(journal, dir.resolve("state"));
        WorkflowContext ctx = new WorkflowContext();
        ctx.setWorkflowId(id);
        ctx.setRequirement("modify existing shortener");
        ctx.setScenario(WorkflowScenario.BROWNFIELD);
        ctx.setStatus(WorkflowStatus.WAITING_FOR_APPROVAL);
        ctx.addAuditEvent("workflow started");
        ctx.addStageOutput("testing", "not executed");
        ctx.addStageOutput("engineeringEvidence", new EngineeringEvidenceService.Evidence(
                "/tmp/GeneratedUrlPolicy.java", "a".repeat(64), "/tmp/test.txt", true, true, 3));
        ctx.getStageApprovals().put(WorkflowState.IMPLEMENTATION, "reviewer1");
        first.save(ctx);
        WorkflowExecutionStore second = new WorkflowExecutionStore(journal, dir.resolve("state"));
        WorkflowContext recovered = second.get(id);
        assertEquals(WorkflowStatus.WAITING_FOR_APPROVAL, recovered.getStatus());
        assertEquals("reviewer1", recovered.getStageApprovals().get(WorkflowState.IMPLEMENTATION));
        assertTrue(recovered.getStageOutputs().get("engineeringEvidence")
                instanceof EngineeringEvidenceService.Evidence);
        assertEquals(1, recovered.getAuditEvents().size());
    }
}
