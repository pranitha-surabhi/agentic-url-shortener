package com.example.agentic.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkflowMetricsResponse {

    private long totalExecutions;

    private long successfulExecutions;

    private long failedExecutions;

    private long rolledBackExecutions;

    private long waitingForApprovalExecutions;

    private long stoppedExecutions;

    private long totalRetries;

    private long totalRollbacks;

    private double successRate;

    private double retryFrequency;

    private double rollbackFrequency;

    private double averageExecutionLatencyMillis;

    private double mttrMillis;
}