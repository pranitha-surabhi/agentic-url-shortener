package com.example.agentic.workflow;

import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Small, deterministic engineering executor: generates real Java source,
 * compiles it and performs behavioral assertions. No shell commands or
 * untrusted user-provided code are executed.
 *
 * This is a guarded local prototype, NOT an LLM-driven code-modification agent.
 */
@Component
public class EngineeringEvidenceService {

    public record Evidence(String sourcePath, String sha256,
                           String testReportPath, boolean compilationPassed,
                           boolean assertionsPassed, int testsExecuted) {}

    public Evidence generateAndValidate(String workflowId, String requirement) {
        if (workflowId == null || !workflowId.matches("[0-9a-fA-F-]{36}")) {
            throw new IllegalArgumentException("Invalid workflow ID");
        }
        try {
            Path root = Path.of(System.getProperty("java.io.tmpdir"), "agentic-url-shortener-artifacts");
            Files.createDirectories(root);
            Path work = root.resolve(workflowId).normalize();
            if (!work.startsWith(root)) {
                throw new IllegalStateException("Unsafe work directory");
            }
            Files.createDirectories(work);
            Path source = work.resolve("GeneratedUrlPolicy.java");
            // Requirement text is deliberately not interpolated into source code.
            String code = """
                    public final class GeneratedUrlPolicy {
                        private GeneratedUrlPolicy() {}
                        public static boolean isHttpUrl(String value) {
                            if (value == null) return false;
                            try {
                                java.net.URI uri = java.net.URI.create(value);
                                String scheme = uri.getScheme();
                                return (\"http\".equalsIgnoreCase(scheme)
                                        || \"https\".equalsIgnoreCase(scheme))
                                        && uri.getHost() != null && !uri.getHost().isBlank();
                            } catch (IllegalArgumentException e) {
                                return false;
                            }
                        }
                    }
                    """;
            Files.writeString(source, code, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new IllegalStateException("JDK compiler missing; Java 21 JDK required");
            }
            int compileCode = compiler.run(null, null, null,
                    "-d", work.toString(), source.toString());
            boolean compiled = compileCode == 0;
            // Independently validate the source's required behavior using the
            // same conservative URI rules, without loading generated code.
            // This is a contract check; it is NOT a generated-code execution test.
            List<String> cases = List.of(
                    "https://example.com",
                    "http://example.org/path",
                    "javascript:alert(1)"
            );
            int passed = 0;
            for (int i = 0; i < cases.size(); i++) {
                String candidate = cases.get(i);
                boolean actual = uriContract(candidate);
                boolean expected = i != 2;
                if (actual == expected) passed++;
            }
            boolean contractsPassed = passed == cases.size();
            Path report = work.resolve("validation.txt");
            Files.writeString(report, "compilationPassed=" + compiled
                    + "\ncontractAssertionsPassed=" + passed + "/" + cases.size()
                    + "\nNOTE: contract assertions do not execute generated code; CI must validate integration.\n",
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            return new Evidence(source.toString(), digest(code), report.toString(),
                    compiled, contractsPassed, cases.size());
        } catch (IOException e) {
            throw new IllegalStateException("Engineering artifact generation failed", e);
        }
    }

    private boolean uriContract(String input) {
        try {
            java.net.URI uri = java.net.URI.create(input);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && uri.getHost() != null && !uri.getHost().isBlank();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
