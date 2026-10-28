package com.example.agentic.controller;

import com.example.agentic.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowController.class)
class WorkflowApprovalSecurityTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean WorkflowService workflowService;

    @Test
    void callerCannotSelfApproveBrownfieldWorkflow() throws Exception {
        mockMvc.perform(post("/api/v1/workflow/execute")
                .param("requirement", "modify existing URL shortener")
                .param("approvalGranted", "true"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(workflowService);
    }
}
