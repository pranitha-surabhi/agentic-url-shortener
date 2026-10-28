package com.example.agentic.dto.response;

import com.example.agentic.workflow.WorkflowScenario;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class WorkflowResponse {

    private String workflowId;

    private WorkflowScenario scenario;

    private String executionStatus;

    private List<String> identifiedTasks;

    private List<String> decisions;

    private List<String> risks;

    private String implementationPlan;

    private String validationReport;

    private String documentationSummary;

    private List<String> artifacts;

    private List<String> auditEvents;

    private Map<String, Object> stageOutputs;

    private int retryCount;

    private boolean approvalRequired;

    private boolean approvalGranted;
}