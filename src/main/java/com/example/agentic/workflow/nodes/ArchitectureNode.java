package com.example.agentic.workflow.nodes;

import com.example.agentic.workflow.*;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ArchitectureNode implements WorkflowNode {
    @Override public String getName() { return "Architecture Design"; }
    @Override public WorkflowState getState() { return WorkflowState.ARCHITECTURE_DESIGN; }

    @Override
    public void execute(WorkflowContext context) {
        String r = context.getRequirement().toLowerCase(Locale.ROOT);
        List<Class<?>> impacted = new ArrayList<>();
        impacted.add(com.example.agentic.service.UrlShortenerService.class);
        impacted.add(com.example.agentic.controller.UrlController.class);
        if (r.contains("analytic") || r.contains("click")) {
            impacted.add(com.example.agentic.service.AnalyticsService.class);
            impacted.add(com.example.agentic.controller.AnalyticsController.class);
        }
        if (r.contains("redirect") || r.contains("faster")) {
            impacted.add(com.example.agentic.controller.RedirectController.class);
        }
        List<String> impactDetails = new ArrayList<>();
        for (Class<?> type : impacted) {
            List<String> methods = new ArrayList<>();
            for (Method method : type.getDeclaredMethods()) {
                if (!method.isSynthetic()) methods.add(method.getName());
            }
            impactDetails.add(type.getName() + " :: " + String.join(", ", methods));
        }
        context.addStageOutput("brownfieldImpact", List.copyOf(impactDetails));
        context.addStageOutput("architecture", "Layered controllers/services/repositories; impacted classes: "
                + String.join("; ", impactDetails));
        context.addDecision("Inspected loaded application classes and method signatures for impact mapping");
        context.addRisk("Impact map uses JVM class signatures, not complete static data-flow analysis");
    }
}
