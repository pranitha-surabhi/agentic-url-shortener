# Agentic Software Engineering System — URL Shortener

## 1. Project Overview

This project is a Java 21 and Spring Boot prototype that demonstrates an **agentic software engineering workflow** around a URL shortener.

The system accepts a high-level engineering requirement and transforms it into a structured, reviewable engineering workflow covering:

1. Requirement Analysis
2. Task Decomposition
3. Architecture Design
4. Implementation
5. Risk Analysis
6. Testing
7. Documentation
8. Release Readiness

The key objective is to demonstrate **controlled agentic execution across the software development lifecycle**, rather than simply implementing a URL-shortener API.

The assessment specifically expects requirement understanding, task decomposition, brownfield reasoning, explicit workflow orchestration, governance, human approval, retries, rollback, safe-stop controls, auditability, reliability metrics, and dynamic replanning. This implementation addresses those areas through an explicit workflow engine and dependency graph.

---

# 2. Technology Stack

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Data JPA
- H2
- Maven
- Lombok
- JUnit 5
- Mockito
- Spring Boot Test

The application is designed to run locally without requiring external infrastructure.

---

# 3. Main Capabilities

The project contains two major areas.

## URL Shortener

The URL-shortener service supports:

- URL creation
- Random short-code generation
- URL redirection
- Expiration handling
- Deletion handling
- Click analytics
- Validation
- Error handling

Custom short-code input is not supported.

Short codes are always generated automatically by the application.

---

## Agentic Workflow Engine

The workflow engine supports:

- Requirement understanding
- Scenario classification
- Task decomposition
- Explicit dependency graph
- Sequential execution
- Parallel execution
- Synchronization barriers
- Cross-stage context
- Decision lineage
- Human approval
- Policy guardrails
- Bounded retries
- Checkpoint-based rollback
- Safe-stop handling
- Audit events
- Reliability metrics
- Dynamic replanning
- Testing
- Documentation
- Release readiness

---

# 4. High-Level Architecture

The application follows a layered architecture.

```text
                         +------------------+
                         |      Client      |
                         +--------+---------+
                                  |
                                  v
                         +------------------+
                         | REST Controllers |
                         +--------+---------+
                                  |
                    +-------------+-------------+
                    |                           |
                    v                           v
           +----------------+          +-------------------+
           | URL Shortener  |          | Workflow Service  |
           |    Service     |          +---------+---------+
           +-------+--------+                    |
                   |                             v
                   v                    +-------------------+
           +----------------+            |  Workflow Engine  |
           |   Repository   |            +---------+---------+
           +----------------+                      |
                                                   v
                                          +-------------------+
                                          |  Workflow Graph   |
                                          +---------+---------+
                                                    |
                     +------------------------------+------------------+
                     |                              |                  |
                     v                              v                  v
             Requirement Analysis          Task Decomposition    Architecture
                                                                         |
                                                  +----------------------+----------------+
                                                  |                                       |
                                                  v                                       v
                                           Implementation                         Risk Analysis
                                             /        \
                                            /          \
                                           v            v
                                      Testing      Documentation
                                           \            /
                                            \          /
                                             v        v
                                         Release Readiness
```

The workflow orchestration layer is intentionally separated from the URL-shortener business logic.

This allows the workflow engine to manage engineering lifecycle activities without tightly coupling workflow decisions to URL-shortener implementation details.

---

# 5. Workflow Architecture

The workflow uses an explicit dependency graph.

```text
Requirement Analysis
        |
        v
Task Decomposition
        |
        v
Architecture Design
       / \
      /   \
     v     v
Implementation   Risk Analysis
   /       \
  v         v
Testing   Documentation
   \         /
    \       /
     v     v
Release Readiness
```

The graph represents dependencies explicitly instead of hard-coding a simple linear sequence.

---

# 6. Workflow Stages

## 6.1 Requirement Analysis

Purpose:

- Understand the incoming requirement
- Determine whether the requirement is actionable
- Identify the scenario
- Normalize the engineering problem

Example output:

```text
Requirement accepted for engineering analysis
```

---

## 6.2 Task Decomposition

The high-level requirement is converted into actionable tasks.

Typical tasks include:

```text
Analyze functional requirements
Define API contract
Design data model
Implement business logic
Implement validation and error handling
Implement automated tests
Prepare documentation
Review existing implementation before modification
```

Tasks are carried in the workflow context so that downstream stages can use the earlier decisions.

---

## 6.3 Architecture Design

The architecture stage determines the implementation structure.

Example decision:

```text
Use layered architecture with Controller, Service and Repository
```

The architecture output is preserved in the workflow context.

---

## 6.4 Implementation

Implementation is controlled by workflow governance.

For brownfield changes, this stage can require explicit human approval before execution.

---

## 6.5 Risk Analysis

Risk analysis identifies functional, lifecycle, concurrency, and change-management risks.

Examples:

```text
Requirement interpretation may require clarification
Existing behavior may be affected by changes
In-memory data is lost when the application restarts
Short-code collisions must be prevented
Expired or deleted URLs must not redirect
Concurrent redirects must not corrupt analytics
```

---

## 6.6 Testing

The testing stage validates the implementation.

Validation includes:

- Unit tests
- Controller tests
- Redirect behavior
- Expiration behavior
- Analytics behavior
- Workflow behavior
- Scenario behavior

---

## 6.7 Documentation

The workflow identifies and prepares documentation artifacts.

Examples:

```text
Project README
Architecture documentation
API documentation
Testing and validation documentation
```

---

## 6.8 Release Readiness

Release Readiness acts as the final synchronization gate.

It cannot complete until the required upstream branches have completed:

```text
Testing
Documentation
Risk Analysis
```

This demonstrates a workflow synchronization barrier.

---

# 7. Sequential and Parallel Execution

The workflow intentionally contains both sequential and parallel execution.

## Sequential

The initial stages execute in dependency order:

```text
Requirement Analysis
        |
        v
Task Decomposition
        |
        v
Architecture Design
```

---

## Parallel

After Architecture Design, independent branches can execute:

```text
             Architecture
              /        \
             /          \
            v            v
    Implementation    Risk Analysis
```

After Implementation:

```text
Implementation
    /       \
   v         v
Testing   Documentation
```

This is an important difference from a simple linear task chain.

---

# 8. Decision Lineage

The workflow maintains a list of decisions.

Examples:

```text
Requirement accepted for engineering analysis
Scenario identified as BROWNFIELD
Use layered architecture with Controller, Service and Repository
Use in-memory storage for the assessment prototype
Separate workflow orchestration from URL-shortener business logic
Validate lifecycle and concurrency risks before release
Implementation is ready for documentation after validation
Workflow is ready for completion
```

This provides traceability into why the workflow took a particular path.

---

# 9. Workflow State Model

The workflow supports the following states:

```text
CREATED
RUNNING
WAITING_FOR_APPROVAL
PAUSED
REPLANNING
COMPLETED
FAILED
ROLLED_BACK
STOPPED
```

The state model allows the engine to represent both normal execution and controlled interruption.

---

# 10. Scenario Classification

The workflow classifies requirements into three scenarios.

## GREENFIELD

Used when a new system or feature is being requested.

Example:

```text
Build a URL shortener
```

Expected:

```text
Scenario: GREENFIELD
Approval Required: false
Execution Status: COMPLETED
```

---

## BROWNFIELD

Used when the requirement indicates modification of an existing system.

Keywords include concepts such as:

```text
existing
modify
update
legacy
```

Example:

```text
Modify the existing URL shortener
```

Expected without approval:

```text
Scenario: BROWNFIELD
Approval Required: true
Approval Granted: false
Execution Status: WAITING_FOR_APPROVAL
```

With approval:

```text
Scenario: BROWNFIELD
Approval Required: true
Approval Granted: true
Execution Status: COMPLETED
```

---

## AMBIGUOUS

Used when the requirement is unclear.

Examples:

```text
Please clarify what needs to be done
```

or:

```text
The requirement is ambiguous
```

Expected:

```text
Scenario: AMBIGUOUS
```

The workflow does not silently assume an unrelated requirement.

---

# 11. Human-in-the-Loop Governance

The system implements controlled autonomy.

Normal engineering stages can execute automatically, while higher-impact changes can require human approval.

For a brownfield implementation:

```text
Architecture
      |
      v
Approval Gate
      |
      +---- Not Approved
      |         |
      |         v
      |   WAITING_FOR_APPROVAL
      |
      +---- Approved
                |
                v
          Implementation
```

This demonstrates the principle:

```text
Agents execute within defined boundaries.
Humans provide oversight and approval for high-impact actions.
```

---

# 12. Approval Behavior

For a brownfield requirement without approval:

```text
Approval Required = true
Approval Granted = false
Status = WAITING_FOR_APPROVAL
```

The audit trail includes:

```text
Approval required before: Implementation
Workflow paused - human approval required before: Implementation
```

Implementation does not execute.

For an approved brownfield requirement:

```text
Approval Required = true
Approval Granted = true
Status = COMPLETED
```

The audit trail includes:

```text
Human approval granted before: Implementation
Started node: Implementation
Completed node: Implementation
```

---

# 13. Workflow Metrics

The application exposes:

```http
GET /api/v1/workflow/metrics
```

Metrics include:

- Total executions
- Successful executions
- Failed executions
- Rolled-back executions
- Waiting-for-approval executions
- Stopped executions
- Total retries
- Total rollbacks
- Success rate
- Retry frequency
- Rollback frequency
- Average execution latency
- MTTR / recovery time

Example response:

```json
{
  "success": true,
  "message": "Workflow metrics retrieved successfully",
  "data": {
    "totalExecutions": 3,
    "successfulExecutions": 2,
    "failedExecutions": 0,
    "rolledBackExecutions": 0,
    "waitingForApprovalExecutions": 1,
    "stoppedExecutions": 0,
    "totalRetries": 0,
    "totalRollbacks": 0,
    "successRate": 66.67,
    "retryFrequency": 0.0,
    "rollbackFrequency": 0.0,
    "averageExecutionLatencyMillis": 0.0,
    "mttrMillis": 0.0
  }
}
```

Actual latency and recovery values depend on execution behavior.

Metrics are maintained in memory and reset when the application restarts.

---

# 14. URL Shortener Architecture

The URL-shortener business logic follows a layered design:

```text
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
In-Memory Storage
```

The workflow orchestration layer is separate:

```text
Workflow Controller
       |
       v
Workflow Service
       |
       v
Workflow Engine
       |
       v
Workflow Graph
```

This separation keeps application functionality and engineering orchestration independent.

---

# 15. URL Shortener Features

The URL shortener supports:

### Create

Create a short URL for an original URL.

### Random Short Code

A short code is generated automatically.

There is no custom short-code input.

### Redirect

The short code resolves to the original URL.

### Expiration

Expired URLs are not redirected.

### Deletion

Deleted/inactive URLs are not redirected.

### Analytics

The application tracks URL access information including click count and access timestamps.

### Validation

Invalid requests are rejected with appropriate validation/error handling.

---

# 16. API — Workflow Execution

Endpoint:

```http
POST /api/v1/workflow/execute
```

Parameters:

```text
requirement
approvalGranted
```

Example:

```text
POST /api/v1/workflow/execute?requirement=Build%20a%20URL%20shortener&approvalGranted=false
```

Brownfield example:

```text
POST /api/v1/workflow/execute?requirement=Modify%20the%20existing%20URL%20shortener&approvalGranted=true
```

---

## 17. Workflow Scenario Tests

The following scenarios have been manually validated:

### Greenfield

```text
Requirement:
Build a URL shortener

Expected:
GREENFIELD
COMPLETED
```

### Brownfield Without Approval

```text
Requirement:
Modify the existing URL shortener

Approval:
false

Expected:
BROWNFIELD
WAITING_FOR_APPROVAL
```

### Brownfield With Approval

```text
Requirement:
Modify the existing URL shortener

Approval:
true

Expected:
BROWNFIELD
COMPLETED
```

### Ambiguous

```text
Requirement:
Please clarify what needs to be done

Expected:
AMBIGUOUS
```

### Dynamic Replanning

An existing workflow is created first.

An upstream stage is changed.

The engine identifies downstream stages and re-executes affected work.

---

# 18. Build Validation

The final project should be validated with:

```bash
mvn clean compile
```

and:

```bash
mvn clean test
```

Expected result:

```text
BUILD SUCCESS
```

The automated test suite has been validated successfully.

---

# 19. Setup Instructions

## Step 1 — Install Java 21

Verify:

```bash
java -version
```

The project is configured for Java 21.

---

## Step 2 — Install Maven

Verify:

```bash
mvn -version
```

Maven 3.9+ is recommended.

---

## Step 3 — Clone/Open the Project

Open the project in VS Code or IntelliJ IDEA.

---

## Step 4 — Compile

```bash
mvn clean compile
```

---

## Step 5 — Run Tests

```bash
mvn clean test
```

---

## Step 6 — Start Application

```bash
mvn spring-boot:run
```

Application:

```text
http://localhost:8080
```

---

# 20. Submission Checklist

Before submitting the project:

```bash
mvn clean test
```

Confirm:

```text
BUILD SUCCESS
```

Then:

```bash
git status
```

Ensure generated build files are not committed.

The following should normally be excluded:

```text
target/
*.class
.idea/
*.iml
.vscode/
```

Recommended repository contents:

```text
README.md
pom.xml
.gitignore
src/
```

---

# 21. Final Engineering Summary

This prototype demonstrates an agentic software engineering workflow that transforms a high-level requirement into a controlled and reviewable engineering outcome.

The implementation demonstrates:

- Requirement understanding
- Scenario classification
- Task decomposition
- Explicit dependency modeling
- Sequential execution
- Parallel execution
- Synchronization barriers
- Cross-stage context
- Decision lineage
- Human approval
- Policy guardrails
- Bounded retries
- Checkpoint-based rollback
- Safe-stop controls
- Auditability
- Reliability metrics
- Dynamic replanning
- URL-shortener functionality
- Automated testing
- Documentation
- Release readiness

The central design principle is:

> **Controlled autonomy — agents execute within defined boundaries, while humans provide oversight, approval, and final quality control.**

The workflow engine is intentionally separated from the URL-shortener business logic so that the orchestration model remains reusable.

The prototype also demonstrates that the workflow is not simply a linear task chain. It uses an explicit dependency graph, parallel execution paths, synchronization barriers, stateful context, governance checkpoints, recovery mechanisms, and dependency-aware dynamic replanning.

The implementation intentionally keeps infrastructure lightweight so that the assessment focuses on engineering judgment, orchestration, validation, governance, and reliability rather than unnecessary infrastructure.

---

# Conclusion

The project provides a runnable Java Spring Boot prototype demonstrating an agentic software engineering lifecycle around a URL shortener.

The design focuses on:

```text
Understand
   ↓
Decompose
   ↓
Design
   ↓
Govern
   ↓
Execute
   ↓
Validate
   ↓
Recover / Replan
   ↓
Document
   ↓
Release
```

The resulting system demonstrates controlled, auditable, dependency-aware engineering automation rather than simple linear task execution.
