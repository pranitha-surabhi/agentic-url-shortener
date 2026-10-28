package com.example.agentic.controller;

import com.example.agentic.used.response.ApiResponse;
import com.example.agentic.dto.response.WorkflowResponse;
import com.example.agentic.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<WorkflowResponse>> execute(
            @RequestParam String requirement,
            @RequestParam(defaultValue = "false")
            boolean approvalGranted) {

        if (approvalGranted) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Approval cannot be granted through workflow execution");
        }

        WorkflowResponse response =
                workflowService.executeWorkflow(
                        requirement,
                        false
                );

        return ResponseEntity.ok(
                ApiResponse.<WorkflowResponse>builder()
                        .success(true)
                        .message(
                                "Workflow executed successfully"
                        )
                        .data(response)
                        .build()
        );
    }
}