package com.example.agentic.workflow;

public class PolicyGuardrail {

    public void validate(
            WorkflowContext context,
            WorkflowState state) {

        if (context.getRequirement() == null
                || context.getRequirement().isBlank()) {

            throw new IllegalStateException(
                    "Policy violation: requirement is missing"
            );
        }

        if (state == WorkflowState.IMPLEMENTATION
                && context.getIdentifiedTasks().isEmpty()) {

            throw new IllegalStateException(
                    "Policy violation: implementation cannot start "
                            + "without task decomposition"
            );
        }

        if (state == WorkflowState.RELEASE_READINESS
                && context.getValidationResults().isEmpty()) {

            throw new IllegalStateException(
                    "Policy violation: release requires validation"
            );
        }
    }
}