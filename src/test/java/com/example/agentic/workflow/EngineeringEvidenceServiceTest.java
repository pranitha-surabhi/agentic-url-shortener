package com.example.agentic.workflow;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class EngineeringEvidenceServiceTest {
    @Test
    void generatesCompilesAndRunsAcceptanceTests() throws Exception {
        EngineeringEvidenceService service = new EngineeringEvidenceService();
        var evidence = service.generateAndValidate(
                UUID.randomUUID().toString(), "Build a URL shortener");
        assertTrue(evidence.compilationPassed());
        assertTrue(evidence.assertionsPassed());
        assertEquals(3, evidence.testsExecuted());
        assertTrue(Files.exists(Path.of(evidence.sourcePath())));
        assertTrue(Files.exists(Path.of(evidence.testReportPath())));
        assertEquals(64, evidence.sha256().length());
    }

    @Test
    void rejectsUnsafeWorkflowId() {
        var service = new EngineeringEvidenceService();
        assertThrows(IllegalArgumentException.class,
                () -> service.generateAndValidate("../escape", "unsafe"));
    }
}
