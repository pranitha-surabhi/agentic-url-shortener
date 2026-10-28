package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.WorkflowContext;
import com.example.agentic.workflow.WorkflowNode;
import com.example.agentic.workflow.WorkflowState;
import org.springframework.stereotype.Component;

@Component
public class ArchitectureNode implements WorkflowNode {

    @Override
    public String getName() {
        return "Architecture Design";
    }

    @Override
    public WorkflowState getState() {
        return WorkflowState.ARCHITECTURE_DESIGN;
    }

    @Override
    public void execute(WorkflowContext context) {

        context.addDecision(
                "Use layered architecture with Controller, Service and Repository"
        );

        context.addDecision(
                "Use in-memory storage for the assessment prototype"
        );

        context.addDecision(
                "Separate workflow orchestration from URL-shortener business logic"
        );

        context.addRisk(
                "In-memory data is lost when the application restarts"
        );

        context.addStageOutput(
                "architecture",
                "Layered Spring Boot architecture with explicit workflow orchestration"
        );

        context.addValidationResult(
                "Architecture reviewed against identified tasks"
        );
    }
}