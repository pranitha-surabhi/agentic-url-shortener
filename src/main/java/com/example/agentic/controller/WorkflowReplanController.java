package com.example.agentic.controller;

import com.example.agentic.used.response.ApiResponse;
import com.example.agentic.dto.request.WorkflowReplanRequest;
import com.example.agentic.dto.response.WorkflowReplanResponse;
import com.example.agentic.workflow.WorkflowEngine;
import com.example.agentic.workflow.WorkflowResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflow")
@RequiredArgsConstructor
public class WorkflowReplanController {

    private final WorkflowEngine workflowEngine;

    @PostMapping("/replan")
    public ResponseEntity<ApiResponse<WorkflowReplanResponse>> replan(
            @RequestBody WorkflowReplanRequest request) {

        WorkflowResult result =
                workflowEngine.replan(
                        request.getWorkflowId(),
                        request.getChangedStage(),
                        request.getNewOutput()
                );

        List<String> affectedStages =
                result.getAuditEvents()
                        .stream()
                        .filter(event ->
                                event.startsWith(
                                        "Affected downstream stage:"
                                )
                        )
                        .map(event ->
                                event.substring(
                                        "Affected downstream stage:"
                                                .length()
                                ).trim()
                        )
                        .toList();

        WorkflowReplanResponse response =
                WorkflowReplanResponse.builder()
                        .workflowId(
                                result.getWorkflowId()
                        )
                        .scenario(
                                result.getScenario()
                        )
                        .executionStatus(
                                result.getStatus().name()
                        )
                        .replanned(true)
                        .changedStage(
                                request.getChangedStage().name()
                        )
                        .newOutput(
                                request.getNewOutput()
                        )
                        .affectedStages(
                                affectedStages
                        )
                        .auditEvents(
                                result.getAuditEvents()
                        )
                        .build();

        return ResponseEntity.ok(
                ApiResponse.<WorkflowReplanResponse>builder()
                        .success(true)
                        .message(
                                "Workflow replanned successfully"
                        )
                        .data(response)
                        .build()
        );
    }
}