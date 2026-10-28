package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.*;
import org.springframework.stereotype.Component;
import java.util.Locale;

@Component
public class RequirementAnalysisNode implements WorkflowNode {
    @Override public String getName() { return "Requirement Analysis"; }
    @Override public WorkflowState getState() { return WorkflowState.REQUIREMENT_ANALYSIS; }

    @Override
    public void execute(WorkflowContext context) {
        String raw = context.getRequirement();
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Requirement cannot be empty");
        }
        String normalized = raw.strip().replaceAll("\\s+", " ");
        if (normalized.length() > 4000) {
            throw new IllegalArgumentException("Requirement exceeds maximum length");
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        context.addStageOutput("requirementAnalysis", normalized);
        context.addDecision("Normalized requirement and classified scenario: " + context.getScenario());
        context.addValidationResult("Requirement is non-empty and within length limit");
        if (context.getScenario() == WorkflowScenario.AMBIGUOUS
                || lower.contains("faster") || lower.contains("better")
                || lower.contains("improve performance")) {
            context.setScenario(WorkflowScenario.AMBIGUOUS);
            context.addRisk("Acceptance criteria are incomplete; clarification required");
            context.addStageOutput("clarificationQuestions",
                    "Specify measurable acceptance criteria, scope, performance baseline and deadline");
            context.addDecision("Proceed with impact analysis only; require human clarification");
        } else {
            context.addStageOutput("acceptanceCriteria",
                    "Generated code must compile; executable acceptance checks must pass; release requires review");
        }
    }
}
