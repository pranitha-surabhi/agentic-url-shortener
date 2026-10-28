package com.example.agentic.workflow;

import com.example.agentic.workflow.nodes.ArchitectureNode;
import com.example.agentic.workflow.nodes.DocumentationNode;
import com.example.agentic.workflow.nodes.ImplementationNode;
import com.example.agentic.workflow.nodes.ReleaseReadinessNode;
import com.example.agentic.workflow.nodes.RequirementAnalysisNode;
import com.example.agentic.workflow.nodes.RiskAnalysisNode;
import com.example.agentic.workflow.nodes.TaskDecompositionNode;
import com.example.agentic.workflow.nodes.TestingNode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowConfiguration {

    @Bean
    public WorkflowGraph workflowGraph(
            RequirementAnalysisNode requirementAnalysisNode,
            TaskDecompositionNode taskDecompositionNode,
            ArchitectureNode architectureNode,
            ImplementationNode implementationNode,
            RiskAnalysisNode riskAnalysisNode,
            TestingNode testingNode,
            DocumentationNode documentationNode,
            ReleaseReadinessNode releaseReadinessNode) {

        WorkflowGraph graph =
                new WorkflowGraph();

        graph.addNode(
                requirementAnalysisNode
        );

        graph.addNode(
                taskDecompositionNode
        );

        graph.addNode(
                architectureNode
        );

        graph.addNode(
                implementationNode
        );

        graph.addNode(
                riskAnalysisNode
        );

        graph.addNode(
                testingNode
        );

        graph.addNode(
                documentationNode
        );

        graph.addNode(
                releaseReadinessNode
        );

        /*
         * Requirement Analysis
         *          ↓
         * Task Decomposition
         *          ↓
         * Architecture
         */
        graph.addDependency(
                WorkflowState.REQUIREMENT_ANALYSIS,
                WorkflowState.TASK_DECOMPOSITION
        );

        graph.addDependency(
                WorkflowState.TASK_DECOMPOSITION,
                WorkflowState.ARCHITECTURE_DESIGN
        );

        /*
         * Architecture
         *      ↓
         * Implementation
         *
         * Architecture
         *      ↓
         * Risk Analysis
         */
        graph.addDependency(
                WorkflowState.ARCHITECTURE_DESIGN,
                WorkflowState.IMPLEMENTATION
        );

        graph.addDependency(
                WorkflowState.ARCHITECTURE_DESIGN,
                WorkflowState.RISK_ANALYSIS
        );

        /*
         * Implementation
         *      ↓
         * Testing
         *
         * Implementation
         *      ↓
         * Documentation
         */
        graph.addDependency(
                WorkflowState.IMPLEMENTATION,
                WorkflowState.TESTING
        );

        graph.addDependency(
                WorkflowState.IMPLEMENTATION,
                WorkflowState.DOCUMENTATION
        );

        /*
         * Synchronization barrier.
         *
         * Release Readiness requires:
         *
         * Testing
         * Documentation
         * Risk Analysis
         */
        graph.addDependency(
                WorkflowState.TESTING,
                WorkflowState.RELEASE_READINESS
        );

        graph.addDependency(
                WorkflowState.DOCUMENTATION,
                WorkflowState.RELEASE_READINESS
        );

        graph.addDependency(
                WorkflowState.RISK_ANALYSIS,
                WorkflowState.RELEASE_READINESS
        );

        return graph;
    }

    @Bean
    public WorkflowEngine workflowEngine(
            WorkflowGraph workflowGraph,
            WorkflowMetrics workflowMetrics,
            WorkflowExecutionStore executionStore) {

        return new WorkflowEngine(
                workflowGraph,
                new RetryPolicy(3),
                new ApprovalGate(),
                new PolicyGuardrail(),
                new SafeStopController(),
                workflowMetrics,
                executionStore
        );
    }
}