package com.example.agentic.workflow;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorkflowExecutionStore {

    private final WorkflowAuditJournal auditJournal;

    public WorkflowExecutionStore() {
        this.auditJournal = null;
    }

    @Autowired
    public WorkflowExecutionStore(WorkflowAuditJournal auditJournal) {
        this.auditJournal = auditJournal;
    }

    private final Map<String, WorkflowContext> workflows =
            new ConcurrentHashMap<>();

    public void save(
            WorkflowContext context) {

        if (context == null
                || context.getWorkflowId() == null) {

            throw new IllegalArgumentException(
                    "Workflow context and workflow ID are required"
            );
        }

        workflows.put(
                context.getWorkflowId(),
                context
        );
        if (auditJournal != null) {
            auditJournal.appendNew(context.getWorkflowId(), context.getAuditEvents());
        }
    }

    public WorkflowContext get(
            String workflowId) {

        if (workflowId == null
                || workflowId.isBlank()) {

            return null;
        }

        return workflows.get(workflowId);
    }

    public boolean contains(
            String workflowId) {

        return workflowId != null
                && workflows.containsKey(workflowId);
    }

    public void remove(
            String workflowId) {

        if (workflowId != null) {
            workflows.remove(workflowId);
        }
    }

    public int size() {
        return workflows.size();
    }
}