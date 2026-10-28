package com.example.agentic.dto.request;

import com.example.agentic.workflow.WorkflowState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkflowReplanRequest {

    @NotBlank
    private String workflowId;

    @NotNull
    private WorkflowState changedStage;

    @NotBlank
    private String newOutput;
}