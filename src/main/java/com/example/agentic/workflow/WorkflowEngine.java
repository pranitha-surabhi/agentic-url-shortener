package com.example.agentic.workflow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WorkflowEngine {

    private final WorkflowGraph graph;

    private final RetryPolicy retryPolicy;

    private final ApprovalGate approvalGate;

    private final PolicyGuardrail policyGuardrail;

    private final SafeStopController safeStopController;

    private final WorkflowMetrics workflowMetrics;

    private final WorkflowExecutionStore executionStore;

    public WorkflowEngine(
            WorkflowGraph graph) {

        this(
                graph,
                new RetryPolicy(3),
                new ApprovalGate(),
                new PolicyGuardrail(),
                new SafeStopController(),
                new WorkflowMetrics(),
                new WorkflowExecutionStore()
        );
    }

    /*
     * Kept for compatibility with existing tests.
     */
    public WorkflowEngine(
            WorkflowGraph graph,
            RetryPolicy retryPolicy,
            ApprovalGate approvalGate,
            PolicyGuardrail policyGuardrail,
            SafeStopController safeStopController) {

        this(
                graph,
                retryPolicy,
                approvalGate,
                policyGuardrail,
                safeStopController,
                new WorkflowMetrics(),
                new WorkflowExecutionStore()
        );
    }

    /*
     * Kept for compatibility with the metrics implementation.
     */
    public WorkflowEngine(
            WorkflowGraph graph,
            RetryPolicy retryPolicy,
            ApprovalGate approvalGate,
            PolicyGuardrail policyGuardrail,
            SafeStopController safeStopController,
            WorkflowMetrics workflowMetrics) {

        this(
                graph,
                retryPolicy,
                approvalGate,
                policyGuardrail,
                safeStopController,
                workflowMetrics,
                new WorkflowExecutionStore()
        );
    }

    /*
     * Main constructor used by Spring.
     */
    public WorkflowEngine(
            WorkflowGraph graph,
            RetryPolicy retryPolicy,
            ApprovalGate approvalGate,
            PolicyGuardrail policyGuardrail,
            SafeStopController safeStopController,
            WorkflowMetrics workflowMetrics,
            WorkflowExecutionStore executionStore) {

        this.graph = graph;
        this.retryPolicy = retryPolicy;
        this.approvalGate = approvalGate;
        this.policyGuardrail = policyGuardrail;
        this.safeStopController = safeStopController;
        this.workflowMetrics = workflowMetrics;
        this.executionStore = executionStore;
    }

    public WorkflowResult execute(
            String requirement,
            WorkflowScenario scenario) {

        return execute(
                requirement,
                scenario,
                false
        );
    }

    public WorkflowResult execute(
            String requirement,
            WorkflowScenario scenario,
            boolean approvalGranted) {

        WorkflowContext context =
                new WorkflowContext();

        context.setWorkflowId(
                UUID.randomUUID().toString()
        );

        context.setRequirement(requirement);

        context.setScenario(scenario);

        context.setApprovalGranted(
                approvalGranted
        );

        context.setStatus(
                WorkflowStatus.RUNNING
        );

        context.setStartedAt(
                LocalDateTime.now()
        );

        workflowMetrics.recordExecutionStarted();

        context.addAuditEvent(
                "Workflow started: "
                        + context.getWorkflowId()
        );

        // Capture initial state before running any engineering nodes.
        executionStore.save(context);

        Set<WorkflowState> completed =
                new HashSet<>();

        WorkflowCheckpoint checkpoint =
                WorkflowCheckpoint.capture(context);

        long recoveryStartTime = -1L;

        long recoveryTimeMillis = -1L;

        try {

            while (
                    completed.size()
                            < graph.getNodes().size()
            ) {

                if (safeStopController.shouldStop(
                        context)) {

                    break;
                }

                if (context.getStatus()
                        == WorkflowStatus.WAITING_FOR_APPROVAL) {

                    break;
                }

                List<WorkflowGraphNode> readyNodes =
                        findReadyNodes(completed);

                if (readyNodes.isEmpty()) {

                    throw new IllegalStateException(
                            "Workflow dependency graph cannot make progress"
                    );
                }

                Set<WorkflowState> executedNodes =
                        executeReadyNodes(
                                readyNodes,
                                context,
                                checkpoint
                        );

                if (context.getStatus()
                        == WorkflowStatus.WAITING_FOR_APPROVAL) {

                    break;
                }

                completed.addAll(
                        executedNodes
                );

                checkpoint =
                        WorkflowCheckpoint.capture(
                                context
                        );
            
                executionStore.save(context);
            }

            if (context.getStatus()
                    == WorkflowStatus.RUNNING
                    && completed.size()
                    == graph.getNodes().size()) {

                if ("BLOCKED".equals(context.getStageOutputs().get("releaseReadiness"))) {
                    context.setStatus(WorkflowStatus.STOPPED);
                    context.addAuditEvent("Release blocked: missing trusted execution evidence");
                } else {
                    context.setCurrentState(WorkflowState.COMPLETED);
                    context.setStatus(WorkflowStatus.COMPLETED);
                }

                context.setCompletedAt(
                        LocalDateTime.now()
                );

                context.addAuditEvent(
                        "Workflow completed successfully"
                );
            }

        } catch (Exception exception) {

            recoveryStartTime =
                    System.currentTimeMillis();

            context.restore(checkpoint);

            context.setStatus(
                    WorkflowStatus.ROLLED_BACK
            );

            context.setCompletedAt(
                    LocalDateTime.now()
            );

            recoveryTimeMillis =
                    System.currentTimeMillis()
                            - recoveryStartTime;

            context.addAuditEvent(
                    "Workflow rolled back after failure: "
                            + exception.getMessage()
            );
        }

        WorkflowResult result =
                buildResult(context);

        workflowMetrics.recordExecutionCompleted(
                result,
                recoveryTimeMillis
        );

        /*
         * Save the latest workflow context so that
         * dynamic replanning can locate it later.
         */
        executionStore.save(context);

        return result;
    }


    /**
     * Resume the existing dependency graph after an authorized human decision.
     * Completed stages are recovered from their persisted stage output keys.
     * Approval is scoped to the specified stage, not the whole workflow.
     */
    public synchronized WorkflowResult approveAndResume(
            String workflowId, WorkflowState approvedStage, String reviewer) {
        WorkflowContext context = executionStore.get(workflowId);
        if (context == null) throw new IllegalArgumentException("Unknown workflow ID");
        if (context.getStatus() != WorkflowStatus.WAITING_FOR_APPROVAL)
            throw new IllegalStateException("Workflow is not awaiting human approval");
        if (reviewer == null || !reviewer.matches("[A-Za-z0-9._-]{3,80}"))
            throw new IllegalArgumentException("Valid reviewer identifier required");
        Set<WorkflowState> completed = new HashSet<>();
        for (WorkflowGraphNode graphNode : graph.getNodes()) {
            WorkflowState state = graphNode.getWorkflowNode().getState();
            if (context.getStageOutputs().containsKey(stageKey(state))) {
                completed.add(state);
            }
        }
        boolean stageReady = findReadyNodes(completed).stream().anyMatch(
                node -> node.getWorkflowNode().getState() == approvedStage);
        if (!stageReady || !approvalGate.requiresApproval(context, approvedStage)) {
            throw new IllegalArgumentException("Stage is not an active approval checkpoint");
        }
        context.getStageApprovals().put(approvedStage, reviewer);
        context.setApprovalGranted(true);
        context.addAuditEvent("Reviewer " + reviewer + " approved stage " + approvedStage);
        executionStore.save(context);
        context.setStatus(WorkflowStatus.RUNNING);
        WorkflowCheckpoint checkpoint = WorkflowCheckpoint.capture(context);
        try {
            while (completed.size() < graph.getNodes().size()
                    && context.getStatus() == WorkflowStatus.RUNNING) {
                if (safeStopController.shouldStop(context)) {
                    context.setStatus(WorkflowStatus.STOPPED);
                    break;
                }
                List<WorkflowGraphNode> ready = findReadyNodes(completed);
                if (ready.isEmpty()) throw new IllegalStateException("Graph made no progress");
                Set<WorkflowState> executed = executeReadyNodes(ready, context, checkpoint);
                completed.addAll(executed);
                if (context.getStatus() == WorkflowStatus.WAITING_FOR_APPROVAL) break;
                checkpoint = WorkflowCheckpoint.capture(context);
            
                executionStore.save(context);
            }
            if (completed.size() == graph.getNodes().size()
                    && context.getStatus() == WorkflowStatus.RUNNING) {
                if ("BLOCKED".equals(context.getStageOutputs().get("releaseReadiness"))) {
                    context.setStatus(WorkflowStatus.STOPPED);
                } else {
                    context.setStatus(WorkflowStatus.COMPLETED);
                    context.setCurrentState(WorkflowState.COMPLETED);
                }
                context.setCompletedAt(LocalDateTime.now());
            }
        } catch (Exception exception) {
            context.restore(checkpoint);
            context.setStatus(WorkflowStatus.ROLLED_BACK);
            context.addAuditEvent("Resume failed and checkpoint restored: "
                    + exception.getClass().getSimpleName());
        }
        executionStore.save(context);
        return buildResult(context);
    }

    /**
     * Dynamically replans a previously executed workflow
     * when an upstream stage output changes.
     */
    public WorkflowResult replan(
            String workflowId,
            WorkflowState changedStage,
            String newOutput) {

        WorkflowContext context =
                executionStore.get(workflowId);

        if (context == null) {

            throw new IllegalArgumentException(
                    "Workflow not found: " + workflowId
            );
        }

        if (changedStage == null) {

            throw new IllegalArgumentException(
                    "Changed stage must not be null"
            );
        }

        if (newOutput == null
                || newOutput.isBlank()) {

            throw new IllegalArgumentException(
                    "New output must not be blank"
            );
        }

        /*
         * Identify all downstream stages affected by
         * the changed upstream stage.
         */
        Set<WorkflowState> affectedStates =
                graph.findDownstreamStates(
                        changedStage
                );

        // Any changed upstream artifact invalidates previously granted approvals.
        context.getStageApprovals().clear();
        context.setApprovalGranted(false);
        context.setStatus(
                WorkflowStatus.REPLANNING
        );

        context.addAuditEvent(
                "Workflow entered REPLANNING state"
        );

        context.addAuditEvent(
                "Upstream output changed: "
                        + changedStage
        );

        /*
         * Store the new upstream output.
         */
        context.addStageOutput(
                stageKey(changedStage),
                newOutput
        );

        /*
         * Record affected downstream stages.
         */
        for (WorkflowState state :
                affectedStates) {

            context.addAuditEvent(
                    "Affected downstream stage: "
                            + state
            );
        }

        /*
         * Remove stale outputs generated by the
         * affected downstream stages.
         */
        for (WorkflowState state :
                affectedStates) {

            context.getStageOutputs()
                    .remove(
                            stageKey(state)
                    );
        }

        context.addAuditEvent(
                "Invalidated affected downstream outputs"
        );

        /*
         * Stages before the changed stage remain valid.
         *
         * The changed stage itself is considered already
         * updated because its new output was supplied by
         * the replan request.
         */
        Set<WorkflowState> completed =
                new HashSet<>();

        for (WorkflowGraphNode node :
                graph.getNodes()) {

            WorkflowState state =
                    node.getWorkflowNode().getState();

            if (state != changedStage
                    && !affectedStates.contains(state)) {

                completed.add(state);
            }
        }

        completed.add(changedStage);

        context.setStatus(
                WorkflowStatus.RUNNING
        );

        context.addAuditEvent(
                "Replanning affected downstream stages"
        );

        WorkflowCheckpoint checkpoint =
                WorkflowCheckpoint.capture(context);

        try {

            while (
                    completed.size()
                            < graph.getNodes().size()
            ) {

                if (safeStopController.shouldStop(
                        context)) {

                    break;
                }

                List<WorkflowGraphNode> readyNodes =
                        findReadyNodes(completed);

                if (readyNodes.isEmpty()) {

                    throw new IllegalStateException(
                            "Replanning cannot make progress"
                    );
                }

                Set<WorkflowState> executedNodes =
                        executeReadyNodes(
                                readyNodes,
                                context,
                                checkpoint
                        );

                /*
                 * Approval is still enforced during
                 * replanning.
                 */
                if (context.getStatus()
                        == WorkflowStatus.WAITING_FOR_APPROVAL) {

                    break;
                }

                completed.addAll(
                        executedNodes
                );

                checkpoint =
                        WorkflowCheckpoint.capture(
                                context
                        );
            
                executionStore.save(context);
            }

            if (context.getStatus()
                    == WorkflowStatus.RUNNING
                    && completed.size()
                    == graph.getNodes().size()) {

                if ("BLOCKED".equals(context.getStageOutputs().get("releaseReadiness"))) {
                    context.setStatus(WorkflowStatus.STOPPED);
                    context.addAuditEvent("Release blocked: missing trusted execution evidence");
                } else {
                    context.setCurrentState(WorkflowState.COMPLETED);
                    context.setStatus(WorkflowStatus.COMPLETED);
                }

                context.setCompletedAt(
                        LocalDateTime.now()
                );

                context.addAuditEvent(
                        "Replanning completed successfully"
                );
            }

        } catch (Exception exception) {

            context.restore(checkpoint);

            context.setStatus(
                    WorkflowStatus.ROLLED_BACK
            );

            context.setCompletedAt(
                    LocalDateTime.now()
            );

            context.addAuditEvent(
                    "Replanning rolled back after failure: "
                            + exception.getMessage()
            );
        }

        WorkflowResult result =
                buildResult(context);

        executionStore.save(context);

        return result;
    }

    private List<WorkflowGraphNode> findReadyNodes(
            Set<WorkflowState> completed) {

        List<WorkflowGraphNode> readyNodes =
                new ArrayList<>();

        for (WorkflowGraphNode node :
                graph.getNodes()) {

            WorkflowState state =
                    node.getWorkflowNode().getState();

            if (completed.contains(state)) {
                continue;
            }

            if (completed.containsAll(
                    node.getDependencies())) {

                readyNodes.add(node);
            }
        }

        return readyNodes;
    }

    private Set<WorkflowState> executeReadyNodes(
            List<WorkflowGraphNode> readyNodes,
            WorkflowContext context,
            WorkflowCheckpoint checkpoint) {

        List<CompletableFuture<WorkflowState>> futures =
                new ArrayList<>();

        for (WorkflowGraphNode node :
                readyNodes) {

            futures.add(
                    CompletableFuture.supplyAsync(
                            () -> {

                                boolean executed =
                                        executeNodeWithControls(
                                                node.getWorkflowNode(),
                                                context,
                                                checkpoint
                                        );

                                if (executed) {

                                    return node
                                            .getWorkflowNode()
                                            .getState();
                                }

                                return null;
                            }
                    )
            );
        }

        CompletableFuture.allOf(
                futures.toArray(
                        new CompletableFuture[0]
                )
        ).join();

        Set<WorkflowState> executedStates =
                new HashSet<>();

        for (CompletableFuture<WorkflowState> future :
                futures) {

            WorkflowState state =
                    future.join();

            if (state != null) {
                executedStates.add(state);
            }
        }

        return executedStates;
    }

    private boolean executeNodeWithControls(
            WorkflowNode node,
            WorkflowContext context,
            WorkflowCheckpoint checkpoint) {

        WorkflowState state =
                node.getState();

        /*
         * Human approval gate.
         */
        if (approvalGate.requiresApproval(
                context,
                state)) {

            context.setStatus(
                    WorkflowStatus.WAITING_FOR_APPROVAL
            );

            context.addAuditEvent(
                    "Approval required before: "
                            + node.getName()
            );

            if (!approvalGate.isApproved(context, state)) {

                context.addAuditEvent(
                        "Workflow paused - human approval required before: "
                                + node.getName()
                );

                return false;
            }

            context.setStatus(
                    WorkflowStatus.RUNNING
            );

            context.addAuditEvent(
                    "Human approval granted before: "
                            + node.getName()
            );
        }

        /*
         * Policy validation.
         */
        policyGuardrail.validate(
                context,
                state
        );

        context.setCurrentState(state);

        context.addAuditEvent(
                "Started node: "
                        + node.getName()
        );

        executeWithRetry(
                node,
                context
        );

        context.addAuditEvent(
                "Completed node: "
                        + node.getName()
        );

        return true;
    }

    private void executeWithRetry(
            WorkflowNode node,
            WorkflowContext context) {

        Exception lastException = null;

        for (int attempt = 1;
             attempt <= retryPolicy.maxAttempts();
             attempt++) {

            try {

                node.execute(context);

                return;

            } catch (Exception exception) {

                lastException = exception;

                context.addAuditEvent(
                        "Node "
                                + node.getName()
                                + " failed on attempt "
                                + attempt
                                + ": "
                                + exception.getMessage()
                );

                if (attempt <
                        retryPolicy.maxAttempts()) {

                    context.setRetryCount(
                            context.getRetryCount() + 1
                    );
                }
            }
        }

        throw new IllegalStateException(
                "Node failed after "
                        + retryPolicy.maxAttempts()
                        + " attempts",
                lastException
        );
    }

    private String stageKey(
            WorkflowState state) {

        return switch (state) {

            case REQUIREMENT_ANALYSIS ->
                    "requirementAnalysis";

            case TASK_DECOMPOSITION ->
                    "taskDecomposition";

            case ARCHITECTURE_DESIGN ->
                    "architecture";

            case IMPLEMENTATION ->
                    "implementation";

            case RISK_ANALYSIS ->
                    "riskAnalysis";

            case TESTING ->
                    "testing";

            case DOCUMENTATION ->
                    "documentation";

            case RELEASE_READINESS ->
                    "releaseReadiness";

            case COMPLETED ->
                    "completed";
        };
    }

    private WorkflowResult buildResult(
            WorkflowContext context) {

        return WorkflowResult.builder()
                .workflowId(
                        context.getWorkflowId()
                )
                .scenario(
                        context.getScenario()
                )
                .status(
                        context.getStatus()
                )
                .identifiedTasks(
                        context.getIdentifiedTasks()
                )
                .decisions(
                        context.getDecisions()
                )
                .risks(
                        context.getRisks()
                )
                .validationResults(
                        context.getValidationResults()
                )
                .artifacts(
                        context.getArtifacts()
                )
                .auditEvents(
                        context.getAuditEvents()
                )
                .stageOutputs(
                        context.getStageOutputs()
                )
                .retryCount(
                        context.getRetryCount()
                )
                .approvalRequired(
                        context.isApprovalRequired()
                )
                .approvalGranted(
                        context.isApprovalGranted()
                )
                .startedAt(
                        context.getStartedAt()
                )
                .completedAt(
                        context.getCompletedAt()
                )
                .build();
    }
}