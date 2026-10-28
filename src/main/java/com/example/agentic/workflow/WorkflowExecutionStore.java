package com.example.agentic.workflow;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorkflowExecutionStore {
    private final WorkflowAuditJournal auditJournal;
    private final Path snapshotDirectory;
    private final Map<String, WorkflowContext> workflows = new ConcurrentHashMap<>();

    /** Isolated in-memory store for standalone unit tests. */
    public WorkflowExecutionStore() {
        this.auditJournal = null;
        this.snapshotDirectory = null;
    }

    @Autowired
    public WorkflowExecutionStore(WorkflowAuditJournal journal,
            @Value("${agentic.workflow.store-dir:data/workflow-state}") String directory) {
        this(journal, Path.of(directory));
    }

    public WorkflowExecutionStore(WorkflowAuditJournal journal, Path directory) {
        this.auditJournal = journal;
        this.snapshotDirectory = directory.toAbsolutePath().normalize();
    }

    public synchronized void save(WorkflowContext context) {
        if (context == null || !validId(context.getWorkflowId())) {
            throw new IllegalArgumentException("Valid workflow context and ID are required");
        }
        // Journal is append-only; the snapshot is an atomic checkpoint.
        if (auditJournal != null) {
            auditJournal.appendNew(context.getWorkflowId(), context.getAuditEvents());
        }
        if (snapshotDirectory != null) {
            WorkflowSnapshotCodec.write(pathFor(context.getWorkflowId()), context);
        }
        workflows.put(context.getWorkflowId(), context);
    }

    public synchronized WorkflowContext get(String workflowId) {
        if (!validId(workflowId)) return null;
        WorkflowContext live = workflows.get(workflowId);
        if (live != null) return live;
        if (snapshotDirectory == null) return null;
        Path path = pathFor(workflowId);
        if (!Files.isRegularFile(path)) return null;
        if (auditJournal != null && !auditJournal.verify(workflowId)) {
            throw new IllegalStateException("Workflow audit journal integrity check failed");
        }
        WorkflowContext loaded = WorkflowSnapshotCodec.read(path);
        if (!workflowId.equals(loaded.getWorkflowId())) {
            throw new IllegalStateException("Workflow snapshot identifier mismatch");
        }
        workflows.put(workflowId, loaded);
        return loaded;
    }

    public boolean contains(String workflowId) {
        return get(workflowId) != null;
    }

    public synchronized void remove(String workflowId) {
        workflows.remove(workflowId);
        // Keep durable checkpoints/audit events for governed retention.
    }

    public int size() { return workflows.size(); }

    private Path pathFor(String id) {
        Path path = snapshotDirectory.resolve(id + ".properties").normalize();
        if (!path.startsWith(snapshotDirectory)) throw new IllegalArgumentException("Unsafe path");
        return path;
    }

    private boolean validId(String value) {
        return value != null && value.matches("[0-9a-fA-F-]{36}");
    }
}
