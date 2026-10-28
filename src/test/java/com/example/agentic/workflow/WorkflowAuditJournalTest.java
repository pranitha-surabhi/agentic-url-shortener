package com.example.agentic.workflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class WorkflowAuditJournalTest {
    @TempDir Path directory;

    @Test
    void persistsAndVerifiesHashChainedEventsAcrossInstances() {
        String workflowId = UUID.randomUUID().toString();
        var first = new WorkflowAuditJournal(directory);
        first.appendNew(workflowId, List.of("start", "requirements accepted"));
        assertTrue(first.verify(workflowId));
        var second = new WorkflowAuditJournal(directory);
        second.appendNew(workflowId,
                List.of("start", "requirements accepted", "human approval required"));
        assertEquals(3, readLineCount(directory.resolve(workflowId + ".log")));
        assertTrue(second.verify(workflowId));
    }

    @Test
    void rejectsPathTraversal() {
        var journal = new WorkflowAuditJournal(directory);
        assertThrows(IllegalArgumentException.class,
                () -> journal.appendNew("../invalid", List.of("x")));
    }

    private long readLineCount(Path path) {
        try (var stream = Files.lines(path)) {
            return stream.count();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
