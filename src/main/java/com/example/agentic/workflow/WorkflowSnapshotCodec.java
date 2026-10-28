package com.example.agentic.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;

/**
 * Explicit, typed snapshot codec. Avoids unsafe Java object deserialization.
 * Properties are confined to the trusted workflow state directory.
 */
public final class WorkflowSnapshotCodec {
    private WorkflowSnapshotCodec() {}

    public static void write(Path path, WorkflowContext ctx) {
        Properties p = new Properties();
        put(p, "workflowId", ctx.getWorkflowId());
        put(p, "requirement", ctx.getRequirement());
        put(p, "scenario", ctx.getScenario() == null ? null : ctx.getScenario().name());
        put(p, "status", ctx.getStatus() == null ? null : ctx.getStatus().name());
        put(p, "currentState", ctx.getCurrentState() == null ? null : ctx.getCurrentState().name());
        put(p, "startedAt", ctx.getStartedAt() == null ? null : ctx.getStartedAt().toString());
        put(p, "completedAt", ctx.getCompletedAt() == null ? null : ctx.getCompletedAt().toString());
        p.setProperty("approvalGranted", String.valueOf(ctx.isApprovalGranted()));
        p.setProperty("safeStopRequested", String.valueOf(ctx.isSafeStopRequested()));
        p.setProperty("retryCount", String.valueOf(ctx.getRetryCount()));
        putList(p, "tasks", ctx.getIdentifiedTasks());
        putList(p, "decisions", ctx.getDecisions());
        putList(p, "risks", ctx.getRisks());
        putList(p, "validations", ctx.getValidationResults());
        putList(p, "artifacts", ctx.getArtifacts());
        putList(p, "events", ctx.getAuditEvents());
        p.setProperty("outputs.size", Integer.toString(ctx.getStageOutputs().size()));
        int i = 0;
        for (var entry : ctx.getStageOutputs().entrySet()) {
            String prefix = "outputs." + i++;
            p.setProperty(prefix + ".key", entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Boolean flag) {
                p.setProperty(prefix + ".type", "boolean");
                p.setProperty(prefix + ".value", String.valueOf(flag));
            } else if (value instanceof EngineeringEvidenceService.Evidence evidence) {
                p.setProperty(prefix + ".type", "evidence");
                put(p, prefix + ".source", evidence.sourcePath());
                put(p, prefix + ".hash", evidence.sha256());
                put(p, prefix + ".report", evidence.testReportPath());
                p.setProperty(prefix + ".compiled", String.valueOf(evidence.compilationPassed()));
                p.setProperty(prefix + ".passed", String.valueOf(evidence.assertionsPassed()));
                p.setProperty(prefix + ".count", String.valueOf(evidence.testsExecuted()));
            } else if (value instanceof List<?> list) {
                p.setProperty(prefix + ".type", "list");
                putList(p, prefix + ".items", list.stream().map(String::valueOf).toList());
            } else {
                p.setProperty(prefix + ".type", "string");
                put(p, prefix + ".value", String.valueOf(value));
            }
        }
        p.setProperty("approvals.size", Integer.toString(ctx.getStageApprovals().size()));
        i = 0;
        for (Map.Entry<WorkflowState, String> a : ctx.getStageApprovals().entrySet()) {
            p.setProperty("approvals." + i + ".stage", a.getKey().name());
            p.setProperty("approvals." + i + ".reviewer", a.getValue());
            i++;
        }
        try {
            Files.createDirectories(path.getParent());
            Path temp = Files.createTempFile(path.getParent(), "snapshot-", ".tmp");
            try {
                try (OutputStream out = Files.newOutputStream(temp,
                        StandardOpenOption.TRUNCATE_EXISTING)) {
                    p.store(out, "Agentic workflow snapshot v1");
                }
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to persist workflow snapshot", ex);
        }
    }

    public static WorkflowContext read(Path path) {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to restore workflow snapshot", ex);
        }
        WorkflowContext ctx = new WorkflowContext();
        ctx.setWorkflowId(required(p, "workflowId"));
        ctx.setRequirement(required(p, "requirement"));
        String scenario = p.getProperty("scenario");
        if (scenario != null) ctx.setScenario(WorkflowScenario.valueOf(scenario));
        String status = p.getProperty("status");
        if (status != null) ctx.setStatus(WorkflowStatus.valueOf(status));
        String state = p.getProperty("currentState");
        if (state != null) ctx.setCurrentState(WorkflowState.valueOf(state));
        String start = p.getProperty("startedAt");
        if (start != null) ctx.setStartedAt(LocalDateTime.parse(start));
        String end = p.getProperty("completedAt");
        if (end != null) ctx.setCompletedAt(LocalDateTime.parse(end));
        ctx.setApprovalGranted(Boolean.parseBoolean(p.getProperty("approvalGranted")));
        ctx.setSafeStopRequested(Boolean.parseBoolean(p.getProperty("safeStopRequested")));
        ctx.setRetryCount(Integer.parseInt(p.getProperty("retryCount", "0")));
        getList(p, "tasks").forEach(ctx::addTask);
        getList(p, "decisions").forEach(ctx::addDecision);
        getList(p, "risks").forEach(ctx::addRisk);
        getList(p, "validations").forEach(ctx::addValidationResult);
        getList(p, "artifacts").forEach(ctx::addArtifact);
        getList(p, "events").forEach(ctx::addAuditEvent);
        int count = checkedCount(p, "outputs.size");
        for (int i = 0; i < count; i++) {
            String prefix = "outputs." + i;
            String key = required(p, prefix + ".key");
            String type = required(p, prefix + ".type");
            Object value = switch (type) {
                case "boolean" -> Boolean.parseBoolean(required(p, prefix + ".value"));
                case "evidence" -> new EngineeringEvidenceService.Evidence(
                        required(p, prefix + ".source"), required(p, prefix + ".hash"),
                        required(p, prefix + ".report"),
                        Boolean.parseBoolean(required(p, prefix + ".compiled")),
                        Boolean.parseBoolean(required(p, prefix + ".passed")),
                        Integer.parseInt(required(p, prefix + ".count")));
                case "list" -> getList(p, prefix + ".items");
                case "string" -> required(p, prefix + ".value");
                default -> throw new IllegalStateException("Unknown snapshot output type");
            };
            ctx.addStageOutput(key, value);
        }
        count = checkedCount(p, "approvals.size");
        for (int i = 0; i < count; i++) {
            String prefix = "approvals." + i;
            ctx.getStageApprovals().put(
                    WorkflowState.valueOf(required(p, prefix + ".stage")),
                    required(p, prefix + ".reviewer"));
        }
        return ctx;
    }

    private static void put(Properties p, String key, String value) {
        if (value != null) p.setProperty(key, value);
    }

    private static String required(Properties p, String key) {
        String result = p.getProperty(key);
        if (result == null) throw new IllegalStateException("Incomplete snapshot field: " + key);
        return result;
    }

    private static void putList(Properties p, String prefix, List<String> items) {
        p.setProperty(prefix + ".size", String.valueOf(items.size()));
        for (int i = 0; i < items.size(); i++) {
            p.setProperty(prefix + "." + i, items.get(i));
        }
    }

    private static List<String> getList(Properties p, String prefix) {
        int count = checkedCount(p, prefix + ".size");
        List<String> result = new ArrayList<>();
        for (int i = 0; i < count; i++) result.add(required(p, prefix + "." + i));
        return result;
    }

    private static int checkedCount(Properties p, String key) {
        int count = Integer.parseInt(p.getProperty(key, "0"));
        if (count < 0 || count > 100000) throw new IllegalStateException("Invalid snapshot size");
        return count;
    }
}
