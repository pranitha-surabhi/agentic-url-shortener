package com.example.agentic.workflow;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.Properties;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class WorkflowMetrics {

    private final Path metricsFile;

    /** Tests and non-Spring callers use isolated in-memory counters. */
    public WorkflowMetrics() {
        this.metricsFile = null;
    }

    @Autowired
    public WorkflowMetrics(@Value("${agentic.metrics.file:data/workflow-metrics.properties}") String file) {
        this.metricsFile = Path.of(file).toAbsolutePath().normalize();
        loadCounters();
    }

    private synchronized void loadCounters() {
        if (metricsFile == null || !Files.isRegularFile(metricsFile)) return;
        try (var in = Files.newInputStream(metricsFile)) {
            Properties p = new Properties();
            p.load(in);
            for (var entry : persistentCounters().entrySet()) {
                entry.getValue().set(Long.parseLong(p.getProperty(entry.getKey(), "0")));
            }
        } catch (IOException | NumberFormatException ex) {
            throw new IllegalStateException("Unable to read durable workflow metrics", ex);
        }
    }

    private synchronized void persistCounters() {
        if (metricsFile == null) return;
        try {
            Path parent = metricsFile.getParent();
            Files.createDirectories(parent);
            Properties props = new Properties();
            for (var entry : persistentCounters().entrySet()) {
                props.setProperty(entry.getKey(), Long.toString(entry.getValue().get()));
            }
            Path temp = Files.createTempFile(parent, "metrics-", ".tmp");
            try {
                try (var out = Files.newOutputStream(temp, StandardOpenOption.TRUNCATE_EXISTING)) {
                    props.store(out, "Durable workflow reliability metrics");
                }
                Files.move(temp, metricsFile, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to persist workflow metrics", ex);
        }
    }

    private java.util.Map<String, AtomicLong> persistentCounters() {
        java.util.Map<String, AtomicLong> values = new java.util.LinkedHashMap<>();
        values.put("totalExecutions", totalExecutions);
        values.put("successfulExecutions", successfulExecutions);
        values.put("failedExecutions", failedExecutions);
        values.put("rolledBackExecutions", rolledBackExecutions);
        values.put("waitingForApprovalExecutions", waitingForApprovalExecutions);
        values.put("stoppedExecutions", stoppedExecutions);
        values.put("totalRetries", totalRetries);
        values.put("totalRollbacks", totalRollbacks);
        values.put("totalExecutionTimeMillis", totalExecutionTimeMillis);
        values.put("completedExecutionSamples", completedExecutionSamples);
        values.put("totalRecoveryTimeMillis", totalRecoveryTimeMillis);
        values.put("recoverySamples", recoverySamples);
        return values;
    }

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
        persistCounters();
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
        persistCounters();
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