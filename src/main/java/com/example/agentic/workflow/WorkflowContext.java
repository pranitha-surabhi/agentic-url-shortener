package com.example.agentic.workflow;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class WorkflowContext {

    private String workflowId;

    private String requirement;

    private WorkflowScenario scenario;

    private WorkflowStatus status;

    private WorkflowState currentState;

    private final List<String> identifiedTasks =
            new ArrayList<>();

    private final List<String> decisions =
            new ArrayList<>();

    private final List<String> risks =
            new ArrayList<>();

    private final List<String> validationResults =
            new ArrayList<>();

    private final List<String> artifacts =
            new ArrayList<>();

    private final List<String> auditEvents =
            new ArrayList<>();

    private final Map<String, Object> stageOutputs =
            new LinkedHashMap<>();

    private int retryCount;

    private boolean approvalRequired;

    private boolean approvalGranted;

    // Explicit stage approvals; re-planning invalidates this map.
    private final Map<WorkflowState, String> stageApprovals = new LinkedHashMap<>();

    private boolean safeStopRequested;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    /**
     * Sets the workflow scenario and determines whether
     * human approval is required.
     *
     * Greenfield:
     *     No approval required.
     *
     * Brownfield:
     *     Approval required because existing behavior
     *     may be affected.
     *
     * Ambiguous:
     *     Approval required because the requirement needs
     *     additional human oversight.
     */
    public synchronized void setScenario(
            WorkflowScenario scenario) {

        this.scenario = scenario;

        this.approvalRequired =
                scenario == WorkflowScenario.BROWNFIELD
                        || scenario == WorkflowScenario.AMBIGUOUS;
    }

    public synchronized void addTask(String task) {
        identifiedTasks.add(task);
    }

    public synchronized void addDecision(String decision) {
        decisions.add(decision);
    }

    public synchronized void addRisk(String risk) {
        risks.add(risk);
    }

    public synchronized void addValidationResult(String result) {
        validationResults.add(result);
    }

    public synchronized void addArtifact(String artifact) {
        artifacts.add(artifact);
    }

    public synchronized void addAuditEvent(String event) {
        auditEvents.add(event);
    }

    public synchronized void addStageOutput(
            String stage,
            Object output) {

        stageOutputs.put(stage, output);
    }

    public synchronized void restore(
            WorkflowCheckpoint checkpoint) {

        identifiedTasks.clear();
        identifiedTasks.addAll(
                checkpoint.tasks()
        );

        decisions.clear();
        decisions.addAll(
                checkpoint.decisions()
        );

        risks.clear();
        risks.addAll(
                checkpoint.risks()
        );

        validationResults.clear();
        validationResults.addAll(
                checkpoint.validations()
        );

        artifacts.clear();
        artifacts.addAll(
                checkpoint.artifacts()
        );

        stageOutputs.clear();
        stageOutputs.putAll(
                checkpoint.stageOutputs()
        );

        addAuditEvent(
                "Workflow state restored to last safe checkpoint"
        );
    }
}