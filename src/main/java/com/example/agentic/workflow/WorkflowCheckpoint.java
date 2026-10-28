package com.example.agentic.workflow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record WorkflowCheckpoint(
        List<String> tasks,
        List<String> decisions,
        List<String> risks,
        List<String> validations,
        List<String> artifacts,
        Map<String, Object> stageOutputs
) {

    public static WorkflowCheckpoint capture(
            WorkflowContext context) {

        return new WorkflowCheckpoint(
                new ArrayList<>(
                        context.getIdentifiedTasks()
                ),
                new ArrayList<>(
                        context.getDecisions()
                ),
                new ArrayList<>(
                        context.getRisks()
                ),
                new ArrayList<>(
                        context.getValidationResults()
                ),
                new ArrayList<>(
                        context.getArtifacts()
                ),
                new LinkedHashMap<>(
                        context.getStageOutputs()
                )
        );
    }
}