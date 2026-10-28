package com.example.agentic.service;

import com.example.agentic.dto.response.WorkflowResponse;
import com.example.agentic.workflow.WorkflowEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkflowEngine workflowEngine;

    private WorkflowService service;

    @BeforeEach
    void setUp() {
        service = new WorkflowService(workflowEngine);
    }

    @Test
    void shouldExecuteEngineeringWorkflow() {

        when(workflowEngine.execute(
                eq("Implement URL shortener"),
                eq(com.example.agentic.workflow.WorkflowScenario.GREENFIELD),
                eq(false)
        )).thenReturn(
                com.example.agentic.workflow.WorkflowResult.builder()
                        .workflowId("workflow-123")
                        .scenario(
                                com.example.agentic.workflow.WorkflowScenario.GREENFIELD
                        )
                        .status(
                                com.example.agentic.workflow.WorkflowStatus.COMPLETED
                        )
                        .identifiedTasks(
                                java.util.List.of(
                                        "Analyze functional requirements",
                                        "Define API contract"
                                )
                        )
                        .decisions(
                                java.util.List.of(
                                        "Requirement accepted"
                                )
                        )
                        .risks(
                                java.util.List.of(
                                        "In-memory storage"
                                )
                        )
                        .validationResults(
                                java.util.List.of(
                                        "Validation completed"
                                )
                        )
                        .artifacts(
                                java.util.List.of(
                                        "URL shortening REST API"
                                )
                        )
                        .auditEvents(
                                java.util.List.of(
                                        "Workflow completed successfully"
                                )
                        )
                        .stageOutputs(
                                java.util.Map.of(
                                        "architecture",
                                        "Layered Spring Boot architecture",
                                        "implementation",
                                        "Implementation completed",
                                        "documentation",
                                        "Documentation completed"
                                )
                        )
                        .retryCount(0)
                        .approvalRequired(false)
                        .approvalGranted(false)
                        .build()
        );

        WorkflowResponse response =
                service.executeWorkflow(
                        "Implement URL shortener", false
                );

        assertNotNull(response);

        assertEquals(
                "workflow-123",
                response.getWorkflowId()
        );

        assertEquals(
                "COMPLETED",
                response.getExecutionStatus()
        );

        assertEquals(
                com.example.agentic.workflow.WorkflowScenario.GREENFIELD,
                response.getScenario()
        );

        assertNotNull(response.getIdentifiedTasks());

        assertNotNull(response.getDecisions());

        assertNotNull(response.getRisks());

        assertNotNull(response.getValidationReport());

        assertNotNull(response.getArtifacts());

        assertNotNull(response.getAuditEvents());

        verify(workflowEngine).execute(
                eq("Implement URL shortener"),
                eq(com.example.agentic.workflow.WorkflowScenario.GREENFIELD),
                eq(false)
        );
    }
}