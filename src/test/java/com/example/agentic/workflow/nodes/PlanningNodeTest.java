package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.WorkflowContext;
import com.example.agentic.workflow.WorkflowScenario;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlanningNodeTest {
    @Test
    void makesAliasTasksSpecificToRequirement() {
        WorkflowContext ctx = new WorkflowContext();
        ctx.setScenario(WorkflowScenario.BROWNFIELD);
        ctx.setRequirement("Modify existing URL shortener to support custom alias");
        new RequirementAnalysisNode().execute(ctx);
        new TaskDecompositionNode().execute(ctx);
        new ArchitectureNode().execute(ctx);
        assertTrue(ctx.getIdentifiedTasks().stream()
                .anyMatch(task -> task.contains("alias uniqueness")));
        assertTrue(ctx.getStageOutputs().containsKey("brownfieldImpact"));
        assertTrue(ctx.isApprovalRequired());
    }

    @Test
    void asksForMeasurableCriteriaForAmbiguousRequirement() {
        WorkflowContext ctx = new WorkflowContext();
        ctx.setScenario(WorkflowScenario.GREENFIELD);
        ctx.setRequirement("Make redirects faster");
        new RequirementAnalysisNode().execute(ctx);
        assertEquals(WorkflowScenario.AMBIGUOUS, ctx.getScenario());
        assertTrue(ctx.getStageOutputs().containsKey("clarificationQuestions"));
    }
}
