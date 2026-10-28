package com.example.agentic.workflow;

public class SafeStopController {

    public void requestStop(
            WorkflowContext context,
            String reason) {

        context.setSafeStopRequested(true);

        context.setStatus(
                WorkflowStatus.STOPPED
        );

        context.addAuditEvent(
                "SAFE_STOP requested: " + reason
        );
    }

    public boolean shouldStop(
            WorkflowContext context) {

        return context.isSafeStopRequested();
    }
}