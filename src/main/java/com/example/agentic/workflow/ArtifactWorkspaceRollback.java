package com.example.agentic.workflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

/** Removes only workflow-generated temporary artifacts after a failed run. */
public final class ArtifactWorkspaceRollback {
    private ArtifactWorkspaceRollback() {}

    public static boolean rollback(String workflowId) {
        if (workflowId == null || !workflowId.matches("[0-9a-fA-F-]{36}")) {
            throw new IllegalArgumentException("Invalid workflow ID");
        }
        Path root = Path.of(System.getProperty("java.io.tmpdir"),
                "agentic-url-shortener-artifacts").toAbsolutePath().normalize();
        Path work = root.resolve(workflowId).normalize();
        if (!work.startsWith(root) || work.equals(root)) {
            throw new IllegalArgumentException("Unsafe rollback target");
        }
        if (!Files.exists(work)) return false;
        try (Stream<Path> paths = Files.walk(work)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
            return true;
        } catch (IOException ex) {
            throw new IllegalStateException("Generated artifact rollback failed", ex);
        }
    }
}
