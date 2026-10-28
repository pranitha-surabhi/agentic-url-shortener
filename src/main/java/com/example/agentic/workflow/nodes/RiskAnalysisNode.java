package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.WorkflowContext;
import com.example.agentic.workflow.WorkflowNode;
import com.example.agentic.workflow.WorkflowState;
import org.springframework.stereotype.Component;

@Component
public class RiskAnalysisNode implements WorkflowNode {

    @Override
    public String getName() {
        return "Risk Analysis";
    }

    @Override
    public WorkflowState getState() {
        return WorkflowState.RISK_ANALYSIS;
    }

    @Override
    public void execute(WorkflowContext context) {

        context.addRisk(
                "In-memory storage loses data after application restart"
        );

        context.addRisk(
                "Short-code collisions must be prevented"
        );

        context.addRisk(
                "Expired or deleted URLs must not redirect"
        );

        context.addRisk(
                "Concurrent redirects must not corrupt analytics"
        );

        context.addDecision(
                "Validate lifecycle and concurrency risks before release"
        );

        context.addStageOutput(
                "riskAnalysis",
                "Functional, lifecycle and concurrency risks identified"
        );

        context.addValidationResult(
                "Risk analysis completed"
        );
    }
}