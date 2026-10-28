package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.EngineeringEvidenceService;
import com.example.agentic.workflow.WorkflowContext;
import com.example.agentic.workflow.WorkflowNode;
import com.example.agentic.workflow.WorkflowState;
import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class DocumentationNode implements WorkflowNode {
    @Override public String getName() { return "Documentation"; }
    @Override public WorkflowState getState() { return WorkflowState.DOCUMENTATION; }

    @Override
    public void execute(WorkflowContext context) {
        Object raw = context.getStageOutputs().get("engineeringEvidence");
        if (!(raw instanceof EngineeringEvidenceService.Evidence evidence)) {
            throw new IllegalStateException("Documentation requires generated artifact provenance");
        }
        Path source = Path.of(evidence.sourcePath());
        Path document = source.getParent().resolve("engineering-summary.md");
        String summary = "# Engineering Workflow Evidence\n\n"
                + "Workflow ID: `" + context.getWorkflowId() + "`\n\n"
                + "Scenario: " + context.getScenario() + "\n\n"
                + "## Planned tasks\n\n"
                + context.getIdentifiedTasks().stream().map(t -> "- " + t.replace("\n"," "))
                    .collect(Collectors.joining("\n"))
                + "\n\n## Source artifact\n\n"
                + "- File: `GeneratedUrlPolicy.java`\n"
                + "- SHA-256: `" + evidence.sha256() + "`\n"
                + "- Compiled: " + evidence.compilationPassed() + "\n"
                + "- Executable acceptance tests: " + evidence.testsExecuted() + "\n\n"
                + "## Limitations\n\n"
                + "This is deterministic generated code in an isolated temporary workspace, "
                + "not a change to the live URL shortener. Human review and CI integration "
                + "remain required before release.\n";
        try {
            Files.writeString(document, summary, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to write engineering summary", ex);
        }
        context.addArtifact("Generated engineering summary: " + document);
        context.addStageOutput("documentation", document.toString());
    }
}
