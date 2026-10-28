package com.example.agentic.workflow;

import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

/**
 * Durable append-only local journal for demo audit events.
 * This protects against accidental truncation/corruption with hash chaining.
 * It is NOT an externally anchored, tamper-proof compliance ledger.
 */
@Component
public class WorkflowAuditJournal {
    private final Path directory;

    public WorkflowAuditJournal() {
        this(Path.of(System.getProperty("agentic.audit.dir", "data/workflow-audit")));
    }

    public WorkflowAuditJournal(Path directory) {
        this.directory = directory.toAbsolutePath().normalize();
    }

    public synchronized void appendNew(String workflowId, List<String> events) {
        if (workflowId == null || !workflowId.matches("[0-9a-fA-F-]{36}")) {
            throw new IllegalArgumentException("Invalid workflow identifier");
        }
        Path path = directory.resolve(workflowId + ".log").normalize();
        if (!path.startsWith(directory)) throw new IllegalArgumentException("Unsafe path");
        try {
            Files.createDirectories(directory);
            List<String> previous = Files.exists(path)
                    ? Files.readAllLines(path, StandardCharsets.UTF_8) : List.of();
            if (previous.size() >= events.size()) return;
            String previousHash = previous.isEmpty() ? "GENESIS"
                    : previous.get(previous.size() - 1).split("\\t", 4)[1];
            StringBuilder output = new StringBuilder();
            for (int i = previous.size(); i < events.size(); i++) {
                String timestamp = Instant.now().toString();
                String payload = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(events.get(i).getBytes(StandardCharsets.UTF_8));
                String hash = sha256(previousHash + "|" + timestamp + "|" + payload);
                output.append(timestamp).append('\t').append(hash)
                        .append('\t').append(payload).append('\n');
                previousHash = hash;
            }
            try (FileChannel channel = FileChannel.open(path,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND)) {
                channel.write(ByteBuffer.wrap(output.toString().getBytes(StandardCharsets.UTF_8)));
                channel.force(true);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to persist workflow audit events", ex);
        }
    }

    public synchronized boolean verify(String workflowId) {
        if (workflowId == null || !workflowId.matches("[0-9a-fA-F-]{36}")) return false;
        Path path = directory.resolve(workflowId + ".log").normalize();
        if (!Files.exists(path)) return false;
        try {
            String previousHash = "GENESIS";
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String[] pieces = line.split("\\t", 3);
                if (pieces.length != 3) return false;
                String calculated = sha256(previousHash + "|" + pieces[0] + "|" + pieces[2]);
                if (!MessageDigest.isEqual(calculated.getBytes(StandardCharsets.UTF_8),
                        pieces[1].getBytes(StandardCharsets.UTF_8))) return false;
                previousHash = pieces[1];
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private String sha256(String input) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
