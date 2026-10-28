package com.example.agentic.dto.response;

import com.example.agentic.workflow.WorkflowScenario;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WorkflowReplanResponse {

    private String workflowId;

    private WorkflowScenario scenario;

    private String executionStatus;

    private boolean replanned;

    private String changedStage;

    private String newOutput;

    private List<String> affectedStages;

    private List<String> auditEvents;
}