package com.example.agentic.workflow;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ArtifactRollbackTest {
    @Test
    void rollbackDeletesOnlyWorkflowGeneratedFiles() throws Exception {
        String id = UUID.randomUUID().toString();
        Path root = Path.of(System.getProperty("java.io.tmpdir"),
                "agentic-url-shortener-artifacts");
        Path work = root.resolve(id);
        Path other = root.resolve("unrelated-" + UUID.randomUUID());
        Files.createDirectories(work.resolve("nested"));
        Files.writeString(work.resolve("nested").resolve("generated.txt"), "generated");
        Files.createDirectories(other);
        try {
            assertTrue(ArtifactWorkspaceRollback.rollback(id));
            assertFalse(Files.exists(work));
            assertTrue(Files.isDirectory(other));
            assertFalse(ArtifactWorkspaceRollback.rollback(id));
        } finally {
            Files.deleteIfExists(other);
        }
    }

    @Test
    void malformedWorkflowIdCannotCauseCleanupOutsideWorkspace() {
        assertThrows(IllegalArgumentException.class,
                () -> ArtifactWorkspaceRollback.rollback("../../"));
    }
}
