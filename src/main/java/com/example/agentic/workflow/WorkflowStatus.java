package com.example.agentic.workflow;

public enum WorkflowStatus {

    CREATED,

    RUNNING,

    WAITING_FOR_APPROVAL,

    PAUSED,

    REPLANNING,

    COMPLETED,

    FAILED,

    ROLLED_BACK,

    STOPPED
}