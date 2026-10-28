package com.example.agentic.workflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ScenarioEndToEndTest {
    @Autowired WorkflowEngine engine;

    @Test
    void greenfieldProducesExecutableCodeAndEvidence() {
        WorkflowResult result = engine.execute(
                "Build a new URL shortener service", WorkflowScenario.GREENFIELD);
        assertEquals(WorkflowStatus.COMPLETED, result.getStatus());
        var evidence = (EngineeringEvidenceService.Evidence)
                result.getStageOutputs().get("engineeringEvidence");
        assertTrue(evidence.compilationPassed());
        assertTrue(evidence.assertionsPassed());
        assertTrue(Files.isRegularFile(Path.of(evidence.sourcePath())));
        assertTrue(Files.isRegularFile(Path.of(evidence.testReportPath())));
        assertTrue(Files.isRegularFile(Path.of(
                evidence.sourcePath()).getParent().resolve("engineering-summary.md")));
    }

    @Test
    void brownfieldCreatesAliasSpecificArtifactAfterApprovals() throws Exception {
        WorkflowResult initial = engine.execute(
                "Modify existing shortener: introduce custom alias",
                WorkflowScenario.BROWNFIELD);
        assertEquals(WorkflowStatus.WAITING_FOR_APPROVAL, initial.getStatus());
        assertTrue(initial.getIdentifiedTasks().stream().anyMatch(t -> t.contains("alias")));
        assertTrue(initial.getStageOutputs().containsKey("brownfieldImpact"));
        WorkflowResult afterImplementation = engine.approveAndResume(
                initial.getWorkflowId(), WorkflowState.IMPLEMENTATION, "reviewer1");
        assertEquals(WorkflowStatus.WAITING_FOR_APPROVAL, afterImplementation.getStatus());
        var evidence = (EngineeringEvidenceService.Evidence)
                afterImplementation.getStageOutputs().get("engineeringEvidence");
        assertTrue(Files.readString(Path.of(evidence.sourcePath())).contains("isValidAlias"));
        assertEquals(5, evidence.testsExecuted());
        WorkflowResult completed = engine.approveAndResume(
                initial.getWorkflowId(), WorkflowState.RELEASE_READINESS, "reviewer1");
        assertEquals(WorkflowStatus.COMPLETED, completed.getStatus());
    }

    @Test
    void ambiguousWorkflowMustPauseAndRecordClarification() {
        WorkflowResult result = engine.execute(
                "Make redirects faster", WorkflowScenario.GREENFIELD);
        assertEquals(WorkflowScenario.AMBIGUOUS, result.getScenario());
        assertEquals(WorkflowStatus.WAITING_FOR_APPROVAL, result.getStatus());
        assertTrue(result.getStageOutputs().containsKey("clarificationQuestions"));
    }
}
