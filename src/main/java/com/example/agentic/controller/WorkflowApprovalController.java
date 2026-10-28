package com.example.agentic.controller;

import com.example.agentic.workflow.ReviewerAuthorization;
import com.example.agentic.workflow.WorkflowEngine;
import com.example.agentic.workflow.WorkflowResult;
import com.example.agentic.workflow.WorkflowState;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workflow")
@RequiredArgsConstructor
public class WorkflowApprovalController {
    private final ReviewerAuthorization authorization;
    private final WorkflowEngine engine;

    @PostMapping("/{workflowId}/approve")
    public Map<String, Object> approve(
            @PathVariable String workflowId,
            @RequestParam WorkflowState stage,
            @RequestHeader(value = "X-Reviewer-Token", required = false) String token,
            @RequestHeader(value = "X-Reviewer-Id", required = false) String reviewer) {
        authorization.verify(token);
        WorkflowResult result = engine.approveAndResume(workflowId, stage, reviewer);
        return Map.of("workflowId", result.getWorkflowId(),
                "status", result.getStatus().name(),
                "approvedStage", stage.name(),
                "approvalRequired", result.isApprovalRequired());
    }

    @PostMapping("/{workflowId}/safe-stop")
    public Map<String, Object> safeStop(
            @PathVariable String workflowId,
            @RequestParam String reason,
            @RequestHeader(value = "X-Reviewer-Token", required = false) String token) {
        authorization.verify(token);
        WorkflowResult result = engine.requestSafeStop(workflowId, reason);
        return Map.of("workflowId", result.getWorkflowId(),
                "status", result.getStatus().name());
    }
}
