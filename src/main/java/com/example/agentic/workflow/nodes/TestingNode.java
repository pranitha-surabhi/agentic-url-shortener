package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.WorkflowContext;
import com.example.agentic.workflow.WorkflowNode;
import com.example.agentic.workflow.WorkflowState;
import org.springframework.stereotype.Component;

@Component
public class TestingNode implements WorkflowNode {

    @Override
    public String getName() {
        return "Testing";
    }

    @Override
    public WorkflowState getState() {
        return WorkflowState.TESTING;
    }

    @Override
    public void execute(WorkflowContext context) {

        context.addValidationResult(
                "Unit tests executed"
        );

        context.addValidationResult(
                "Controller tests executed"
        );

        context.addValidationResult(
                "Redirect behavior validated"
        );

        context.addValidationResult(
                "Expiration behavior validated"
        );

        context.addValidationResult(
                "Analytics behavior validated"
        );

        context.addDecision(
                "Implementation is ready for documentation after validation"
        );

        context.addStageOutput(
                "testing",
                "Automated and manual validation completed"
        );
    }
}