package com.example.agentic.controller;

import com.example.agentic.used.response.ApiResponse;
import com.example.agentic.dto.response.WorkflowMetricsResponse;
import com.example.agentic.workflow.WorkflowMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workflow")
@RequiredArgsConstructor
public class WorkflowMetricsController {

    private final WorkflowMetrics workflowMetrics;

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<WorkflowMetricsResponse>> metrics() {

        WorkflowMetricsResponse response =
                WorkflowMetricsResponse.builder()
                        .totalExecutions(
                                workflowMetrics.getTotalExecutions()
                        )
                        .successfulExecutions(
                                workflowMetrics.getSuccessfulExecutions()
                        )
                        .failedExecutions(
                                workflowMetrics.getFailedExecutions()
                        )
                        .rolledBackExecutions(
                                workflowMetrics.getRolledBackExecutions()
                        )
                        .waitingForApprovalExecutions(
                                workflowMetrics
                                        .getWaitingForApprovalExecutions()
                        )
                        .stoppedExecutions(
                                workflowMetrics.getStoppedExecutions()
                        )
                        .totalRetries(
                                workflowMetrics.getTotalRetries()
                        )
                        .totalRollbacks(
                                workflowMetrics.getTotalRollbacks()
                        )
                        .successRate(
                                workflowMetrics.getSuccessRate()
                        )
                        .retryFrequency(
                                workflowMetrics.getRetryFrequency()
                        )
                        .rollbackFrequency(
                                workflowMetrics.getRollbackFrequency()
                        )
                        .averageExecutionLatencyMillis(
                                workflowMetrics
                                        .getAverageExecutionLatencyMillis()
                        )
                        .mttrMillis(
                                workflowMetrics.getMttrMillis()
                        )
                        .build();

        return ResponseEntity.ok(
                ApiResponse.<WorkflowMetricsResponse>builder()
                        .success(true)
                        .message(
                                "Workflow metrics retrieved successfully"
                        )
                        .data(response)
                        .build()
        );
    }
}