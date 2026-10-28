package com.example.agentic.service;

import com.example.agentic.dto.response.WorkflowResponse;
import com.example.agentic.workflow.WorkflowEngine;
import com.example.agentic.workflow.WorkflowResult;
import com.example.agentic.workflow.WorkflowScenario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowEngine workflowEngine;

    public WorkflowResponse executeWorkflow(
        String requirement,
        boolean approvalGranted) {

        WorkflowScenario scenario =
                determineScenario(requirement);

        WorkflowResult result = workflowEngine.execute(
                requirement,
                scenario,
                approvalGranted
        );

        return WorkflowResponse.builder()
                .workflowId(result.getWorkflowId())
                .scenario(result.getScenario())
                .executionStatus(
                        result.getStatus().name()
                )
                .identifiedTasks(
                        result.getIdentifiedTasks()
                )
                .decisions(
                        result.getDecisions()
                )
                .risks(
                        result.getRisks()
                )
                .implementationPlan(
                        buildImplementationPlan(result)
                )
                .validationReport(
                        buildValidationReport(result)
                )
                .documentationSummary(
                        buildDocumentationSummary(result)
                )
                .artifacts(
                        result.getArtifacts()
                )
                .auditEvents(
                        result.getAuditEvents()
                )
                .stageOutputs(
                        result.getStageOutputs()
                )
                .retryCount(
                        result.getRetryCount()
                )
                .approvalRequired(
                        result.isApprovalRequired()
                )
                .approvalGranted(
                        result.isApprovalGranted()
                )
                .build();
    }

    private WorkflowScenario determineScenario(
            String requirement) {

        if (requirement == null ||
                requirement.isBlank()) {

            return WorkflowScenario.AMBIGUOUS;
        }

        String normalized =
                requirement.toLowerCase();

        if (normalized.contains("existing")
                || normalized.contains("modify")
                || normalized.contains("update")
                || normalized.contains("legacy")) {

            return WorkflowScenario.BROWNFIELD;
        }

        if (normalized.contains("unclear")
                || normalized.contains("ambiguous")
                || normalized.contains("not sure")
                || normalized.contains("clarify")) {

            return WorkflowScenario.AMBIGUOUS;
        }

        return WorkflowScenario.GREENFIELD;
    }

    private String buildImplementationPlan(
            WorkflowResult result) {

        Object architecture =
                result.getStageOutputs()
                        .get("architecture");

        Object implementation =
                result.getStageOutputs()
                        .get("implementation");

        return "Architecture: "
                + String.valueOf(architecture)
                + System.lineSeparator()
                + "Implementation: "
                + String.valueOf(implementation);
    }

    private String buildValidationReport(
            WorkflowResult result) {

        if (result.getValidationResults() == null
                || result.getValidationResults().isEmpty()) {

            return "No validation results available.";
        }

        return String.join(
                System.lineSeparator(),
                result.getValidationResults()
        );
    }

    private String buildDocumentationSummary(
            WorkflowResult result) {

        Object documentation =
                result.getStageOutputs()
                        .get("documentation");

        return String.valueOf(documentation);
    }
}