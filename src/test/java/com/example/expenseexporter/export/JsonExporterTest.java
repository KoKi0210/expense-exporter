package com.example.expenseexporter.export;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static com.example.expenseexporter.export.ExportFixtures.approvedEur;
import static com.example.expenseexporter.export.ExportFixtures.rejectedUsd;
import static com.example.expenseexporter.export.ExportFixtures.withTexts;
import static org.assertj.core.api.Assertions.assertThat;

class JsonExporterTest {

    // Jackson is used only here, to check that the hand-written output is valid JSON.
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .build();

    private final JsonExporter exporter = new JsonExporter();

    @Test
    void exportsInvoicesAndSummary() {
        String json = exporter.export(List.of(approvedEur(), rejectedUsd()));

        assertThat(json).isEqualTo("""
                {
                  "invoices": [
                    {
                      "invoiceNumber": "INV-0001",
                      "issueDate": "2026-10-02",
                      "employeeId": "E-001",
                      "employeeName": "Anna Schmidt",
                      "position": "JUNIOR",
                      "vendor": "Office Depot",
                      "description": "Notebooks, pens",
                      "amount": 45.50,
                      "currency": "EUR",
                      "limit": 300.00,
                      "status": "APPROVED",
                      "rule": "Fixed amount: 300.00",
                      "message": null
                    },
                    {
                      "invoiceNumber": "INV-0002",
                      "issueDate": "2026-10-05",
                      "employeeId": "E-002",
                      "employeeName": "Maria O'Neil",
                      "position": "SENIOR",
                      "vendor": "Smith & Sons",
                      "description": "Taxi \\"airport\\"",
                      "amount": 210.00,
                      "currency": "USD",
                      "limit": 200.00,
                      "status": "REJECTED",
                      "rule": "5% of salary",
                      "message": "Exceeds limit by 10.00"
                    }
                  ],
                  "summary": {
                    "count": 2,
                    "approved": 1,
                    "rejected": 1,
                    "errors": 0,
                    "totalsByCurrency": {
                      "EUR": 45.50,
                      "USD": 210.00
                    }
                  }
                }
                """);
    }

    @Test
    void emptyListIsValidJson() {
        String json = exporter.export(List.of());

        assertThat(json).isEqualTo("""
                {
                  "invoices": [],
                  "summary": {
                    "count": 0,
                    "approved": 0,
                    "rejected": 0,
                    "errors": 0,
                    "totalsByCurrency": {}
                  }
                }
                """);
        assertThat(MAPPER.readTree(json).get("invoices").isEmpty()).isTrue();
    }

    @Test
    void parsedOutputKeepsValuesUnchanged() {
        String vendor = "Smith & Sons <Ltd> 'A'";
        String description = "Line 1\nLine 2\r\nTab\there \"quoted\" back\\slash \u0001 Кафе €";

        JsonNode root = MAPPER.readTree(exporter.export(List.of(withTexts(vendor, description), rejectedUsd())));

        JsonNode first = root.get("invoices").get(0);
        assertThat(first.get("vendor").asString()).isEqualTo(vendor);
        assertThat(first.get("description").asString()).isEqualTo(description);
        assertThat(first.get("amount").decimalValue()).isEqualTo(new BigDecimal("10.00"));
        assertThat(first.get("limit").isNull()).isTrue();
        assertThat(first.get("message").isNull()).isTrue();
        assertThat(root.get("summary").get("totalsByCurrency").get("USD").decimalValue())
                .isEqualTo(new BigDecimal("210.00"));
    }

    static Stream<Arguments> escapedDescriptions() {
        return Stream.of(
                Arguments.of("plain", "\"plain\""),
                Arguments.of("", "\"\""),
                Arguments.of("a\"b", "\"a\\\"b\""),
                Arguments.of("C:\\dir", "\"C:\\\\dir\""),
                Arguments.of("\b\f\n\r\t", "\"\\b\\f\\n\\r\\t\""),
                Arguments.of("x\u0001y", "\"x\\u0001y\""),
                Arguments.of("\u001F", "\"\\u001f\""),
                Arguments.of("/ < & '", "\"/ < & '\""),
                Arguments.of("Кафе €", "\"Кафе €\""));
    }

    @ParameterizedTest
    @MethodSource("escapedDescriptions")
    void escapesDescription(String input, String expected) {
        String json = exporter.export(List.of(withTexts("Vendor", input)));

        assertThat(json).contains("\"description\": " + expected);
        assertThat(MAPPER.readTree(json).get("invoices").get(0).get("description").asString())
                .isEqualTo(input);
    }

    @Test
    void reportsItsFormat() {
        assertThat(exporter.format()).isEqualTo(ExportFormat.JSON);
    }
}
