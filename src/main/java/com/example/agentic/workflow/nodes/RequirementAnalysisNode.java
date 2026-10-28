package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.WorkflowContext;
import com.example.agentic.workflow.WorkflowNode;
import com.example.agentic.workflow.WorkflowState;
import org.springframework.stereotype.Component;

@Component
public class RequirementAnalysisNode implements WorkflowNode {

    @Override
    public String getName() {
        return "Requirement Analysis";
    }

    @Override
    public WorkflowState getState() {
        return WorkflowState.REQUIREMENT_ANALYSIS;
    }

    @Override
    public void execute(WorkflowContext context) {

        String requirement = context.getRequirement();

        if (requirement == null || requirement.isBlank()) {
            throw new IllegalArgumentException(
                    "Requirement cannot be empty"
            );
        }

        context.addDecision(
                "Requirement accepted for engineering analysis"
        );

        context.addDecision(
                "Scenario identified as "
                        + context.getScenario()
        );

        context.addRisk(
                "Requirement interpretation may require clarification"
        );

        context.addStageOutput(
                "requirementAnalysis",
                requirement
        );

        context.addValidationResult(
                "Requirement presence validated"
        );
    }
}