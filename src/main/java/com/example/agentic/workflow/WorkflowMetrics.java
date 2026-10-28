package com.example.agentic.workflow;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class WorkflowMetrics {

    private final AtomicLong totalExecutions =
            new AtomicLong();

    private final AtomicLong successfulExecutions =
            new AtomicLong();

    private final AtomicLong failedExecutions =
            new AtomicLong();

    private final AtomicLong rolledBackExecutions =
            new AtomicLong();

    private final AtomicLong waitingForApprovalExecutions =
            new AtomicLong();

    private final AtomicLong stoppedExecutions =
            new AtomicLong();

    private final AtomicLong totalRetries =
            new AtomicLong();

    private final AtomicLong totalRollbacks =
            new AtomicLong();

    private final AtomicLong totalExecutionTimeMillis =
            new AtomicLong();

    private final AtomicLong completedExecutionSamples =
            new AtomicLong();

    private final AtomicLong totalRecoveryTimeMillis =
            new AtomicLong();

    private final AtomicLong recoverySamples =
            new AtomicLong();

    public void recordExecutionStarted() {
        totalExecutions.incrementAndGet();
    }

    public void recordExecutionCompleted(
            WorkflowResult result,
            long recoveryTimeMillis) {

        WorkflowStatus status =
                result.getStatus();

        if (status == null) {
            return;
        }

        switch (status) {

            case COMPLETED ->
                    successfulExecutions.incrementAndGet();

            case FAILED ->
                    failedExecutions.incrementAndGet();

            case ROLLED_BACK -> {
                rolledBackExecutions.incrementAndGet();
                totalRollbacks.incrementAndGet();
            }

            case WAITING_FOR_APPROVAL ->
                    waitingForApprovalExecutions.incrementAndGet();

            case STOPPED ->
                    stoppedExecutions.incrementAndGet();

            default -> {
                // CREATED, RUNNING, PAUSED and REPLANNING
                // are intermediate states.
            }
        }

        totalRetries.addAndGet(
                result.getRetryCount()
        );

        LocalDateTime startedAt =
                result.getStartedAt();

        LocalDateTime completedAt =
                result.getCompletedAt();

        /*
         * Latency is calculated only for workflows
         * that actually reached a completed terminal
         * state.
         */
        if (startedAt != null
                && completedAt != null
                && (status == WorkflowStatus.COMPLETED
                || status == WorkflowStatus.FAILED
                || status == WorkflowStatus.ROLLED_BACK
                || status == WorkflowStatus.STOPPED)) {

            long duration =
                    Duration.between(
                            startedAt,
                            completedAt
                    ).toMillis();

            if (duration >= 0) {

                totalExecutionTimeMillis
                        .addAndGet(duration);

                completedExecutionSamples
                        .incrementAndGet();
            }
        }

        /*
         * Recovery time is supplied by WorkflowEngine
         * when a rollback occurs.
         */
        if (status == WorkflowStatus.ROLLED_BACK
                && recoveryTimeMillis >= 0) {

            totalRecoveryTimeMillis
                    .addAndGet(recoveryTimeMillis);

            recoverySamples
                    .incrementAndGet();
        }
    }

    public long getTotalExecutions() {
        return totalExecutions.get();
    }

    public long getSuccessfulExecutions() {
        return successfulExecutions.get();
    }

    public long getFailedExecutions() {
        return failedExecutions.get();
    }

    public long getRolledBackExecutions() {
        return rolledBackExecutions.get();
    }

    public long getWaitingForApprovalExecutions() {
        return waitingForApprovalExecutions.get();
    }

    public long getStoppedExecutions() {
        return stoppedExecutions.get();
    }

    public long getTotalRetries() {
        return totalRetries.get();
    }

    public long getTotalRollbacks() {
        return totalRollbacks.get();
    }

    public double getSuccessRate() {

        long total =
                totalExecutions.get();

        if (total == 0) {
            return 0.0;
        }

        return successfulExecutions.get()
                * 100.0
                / total;
    }

    public double getRetryFrequency() {

        long total =
                totalExecutions.get();

        if (total == 0) {
            return 0.0;
        }

        return totalRetries.get()
                * 1.0
                / total;
    }

    public double getRollbackFrequency() {

        long total =
                totalExecutions.get();

        if (total == 0) {
            return 0.0;
        }

        return totalRollbacks.get()
                * 100.0
                / total;
    }

    public double getAverageExecutionLatencyMillis() {

        long samples =
                completedExecutionSamples.get();

        if (samples == 0) {
            return 0.0;
        }

        return totalExecutionTimeMillis.get()
                * 1.0
                / samples;
    }

    public double getMttrMillis() {

        long samples =
                recoverySamples.get();

        if (samples == 0) {
            return 0.0;
        }

        return totalRecoveryTimeMillis.get()
                * 1.0
                / samples;
    }
}