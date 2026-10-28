package com.example.agentic.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecution {

    private String workflowId;

    private String requirement;

    private List<String> identifiedTasks;

    private String implementationPlan;

    private String validationReport;

    private String documentationSummary;

    private String executionStatus;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

}