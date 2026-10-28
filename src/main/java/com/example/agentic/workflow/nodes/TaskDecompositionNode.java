package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.WorkflowContext;
import com.example.agentic.workflow.WorkflowNode;
import com.example.agentic.workflow.WorkflowScenario;
import com.example.agentic.workflow.WorkflowState;
import org.springframework.stereotype.Component;

@Component
public class TaskDecompositionNode implements WorkflowNode {

    @Override
    public String getName() {
        return "Task Decomposition";
    }

    @Override
    public WorkflowState getState() {
        return WorkflowState.TASK_DECOMPOSITION;
    }

    @Override
    public void execute(WorkflowContext context) {

        context.addTask("Analyze functional requirements");

        context.addTask("Define API contract");

        context.addTask("Design data model");

        context.addTask("Implement business logic");

        context.addTask("Implement validation and error handling");

        context.addTask("Implement automated tests");

        context.addTask("Prepare documentation");

        if (context.getScenario() == WorkflowScenario.BROWNFIELD) {

            context.addTask(
                    "Review existing implementation before modification"
            );

            context.addRisk(
                    "Existing behavior may be affected by changes"
            );
        }

        if (context.getScenario() == WorkflowScenario.AMBIGUOUS) {

            context.setApprovalRequired(true);

            context.addRisk(
                    "Ambiguous requirement requires clarification"
            );

            context.addDecision(
                    "Human approval checkpoint required before implementation"
            );
        }

        context.addStageOutput(
                "taskDecomposition",
                context.getIdentifiedTasks()
        );
    }
}