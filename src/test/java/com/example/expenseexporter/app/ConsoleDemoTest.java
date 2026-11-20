package com.example.expenseexporter.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleDemoTest {

    @Test
    void validatesSampleDataAndWritesAllThreeFilesWithoutSpring(@TempDir Path outputDir) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            ConsoleDemo.main(new String[] {outputDir.toString()});
        } finally {
            System.setOut(original);
        }

        assertThat(buffer.toString(StandardCharsets.UTF_8))
                .contains("=== Rules ===", "=== Validation ===", "APPROVED", "REJECTED");
        assertThat(Files.readString(outputDir.resolve("invoices.csv"), StandardCharsets.UTF_8))
                .startsWith("invoiceNumber,");
        assertThat(Files.readString(outputDir.resolve("invoices.json"), StandardCharsets.UTF_8))
                .startsWith("{");
        assertThat(Files.readString(outputDir.resolve("invoices.xml"), StandardCharsets.UTF_8))
                .startsWith("<?xml");
    }
}
