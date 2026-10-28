package com.example.agentic.workflow;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class WorkflowResult {

    private String workflowId;

    private WorkflowScenario scenario;

    private WorkflowStatus status;

    private List<String> identifiedTasks;

    private List<String> decisions;

    private List<String> risks;

    private List<String> validationResults;

    private List<String> artifacts;

    private List<String> auditEvents;

    private Map<String, Object> stageOutputs;

    private int retryCount;

    private boolean approvalRequired;

    private boolean approvalGranted;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}