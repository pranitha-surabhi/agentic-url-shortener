package com.example.agentic.workflow;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorkflowGraph {

    private final Map<WorkflowState, WorkflowGraphNode> nodes =
            new EnumMap<>(WorkflowState.class);

    public void addNode(
            WorkflowNode workflowNode) {

        nodes.put(
                workflowNode.getState(),
                new WorkflowGraphNode(workflowNode)
        );
    }

    public void addDependency(
            WorkflowState from,
            WorkflowState to) {

        WorkflowGraphNode targetNode =
                nodes.get(to);

        if (targetNode == null) {

            throw new IllegalArgumentException(
                    "Workflow node not found: " + to
            );
        }

        targetNode.addDependency(from);
    }

    public WorkflowGraphNode getNode(
            WorkflowState state) {

        WorkflowGraphNode node =
                nodes.get(state);

        if (node == null) {

            throw new IllegalArgumentException(
                    "Workflow node not found: "
                            + state
            );
        }

        return node;
    }

    public Collection<WorkflowGraphNode> getNodes() {
        return nodes.values();
    }

    /**
     * Finds every node directly or indirectly
     * downstream from the changed stage.
     */
    public Set<WorkflowState> findDownstreamStates(
            WorkflowState changedState) {

        Set<WorkflowState> affected =
                new HashSet<>();

        boolean changed;

        do {

            changed = false;

            for (WorkflowGraphNode node :
                    nodes.values()) {

                WorkflowState candidate =
                        node.getWorkflowNode()
                                .getState();

                if (candidate == changedState
                        || affected.contains(candidate)) {

                    continue;
                }

                for (WorkflowState dependency :
                        node.getDependencies()) {

                    if (dependency == changedState
                            || affected.contains(dependency)) {

                        if (affected.add(candidate)) {
                            changed = true;
                        }

                        break;
                    }
                }
            }

        } while (changed);

        return affected;
    }
}