package com.example.agentic.controller;

import com.example.agentic.dto.response.WorkflowResponse;
import com.example.agentic.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowController.class)
class WorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkflowService workflowService;

    @Test
    void shouldExecuteWorkflow() throws Exception {

        WorkflowResponse response = WorkflowResponse.builder()
                .workflowId("workflow-123")
                .executionStatus("COMPLETED")
                .identifiedTasks(List.of(
                        "Understand requirement",
                        "Break down tasks",
                        "Design solution",
                        "Implement code",
                        "Validate implementation",
                        "Generate documentation"
                ))
                .implementationPlan(
                        "Implement REST APIs using Spring Boot and in-memory storage."
                )
                .validationReport(
                        "URL validation and API validation completed."
                )
                .documentationSummary(
                        "README and API documentation prepared."
                )
                .build();

        when(workflowService.executeWorkflow(
                eq("Implement URL shortener"),
                eq(false)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/workflow/execute")
                                .param(
                                        "requirement",
                                        "Implement URL shortener"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.message")
                                .value("Workflow executed successfully")
                )
                .andExpect(
                        jsonPath("$.data.workflowId")
                                .value("workflow-123")
                )
                .andExpect(
                        jsonPath("$.data.executionStatus")
                                .value("COMPLETED")
                )
                .andExpect(
                        jsonPath("$.data.identifiedTasks.length()")
                                .value(6)
                );
    }
}