package com.example.agentic.repository;

import com.example.agentic.models.WorkflowExecution;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class WorkflowRepository {

    private final Map<String, WorkflowExecution> workflowStore =
            new ConcurrentHashMap<>();

    public WorkflowExecution save(WorkflowExecution workflowExecution) {

        workflowStore.put(
                workflowExecution.getWorkflowId(),
                workflowExecution
        );

        return workflowExecution;
    }

    public Optional<WorkflowExecution> findById(String workflowId) {

        return Optional.ofNullable(workflowStore.get(workflowId));
    }

    public Collection<WorkflowExecution> findAll() {

        return workflowStore.values();
    }

}