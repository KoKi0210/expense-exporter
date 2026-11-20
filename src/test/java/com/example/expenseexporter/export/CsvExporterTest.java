package com.example.expenseexporter.export;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.expenseexporter.export.ExportFixtures.approvedEur;
import static com.example.expenseexporter.export.ExportFixtures.rejectedUsd;
import static com.example.expenseexporter.export.ExportFixtures.withTexts;
import static org.assertj.core.api.Assertions.assertThat;

class CsvExporterTest {

    private static final String HEADER = "invoiceNumber,issueDate,employeeId,employeeName,position,vendor,"
            + "description,amount,currency,limit,status,rule,message\r\n";

    private final CsvExporter exporter = new CsvExporter();

    @Test
    void exportsHeaderAndOneCrlfLinePerRecord() {
        String csv = exporter.export(List.of(approvedEur(), rejectedUsd()));

        assertThat(csv).isEqualTo(HEADER
                + "INV-0001,2026-10-02,E-001,Anna Schmidt,JUNIOR,Office Depot,\"Notebooks, pens\","
                + "45.50,EUR,300.00,APPROVED,Fixed amount: 300.00,\r\n"
                + "INV-0002,2026-10-05,E-002,Maria O'Neil,SENIOR,Smith & Sons,\"Taxi \"\"airport\"\"\","
                + "210.00,USD,200.00,REJECTED,5% of salary,Exceeds limit by 10.00\r\n");
    }

    @Test
    void emptyListGivesHeaderOnly() {
        assertThat(exporter.export(List.of())).isEqualTo(HEADER);
    }

    @Test
    void missingLimitIsEmptyField() {
        String csv = exporter.export(List.of(withTexts("Vendor", "Desc")));

        assertThat(csv).endsWith(",10.00,EUR,,APPROVED,test rule,\r\n");
    }

    @Test
    void lineBreakInsideFieldIsQuotedAndKept() {
        String csv = exporter.export(List.of(withTexts("Hotel", "2 nights\nincl. breakfast")));

        assertThat(csv).contains(",Hotel,\"2 nights\nincl. breakfast\",");
    }

    @Test
    void reportsItsFormat() {
        assertThat(exporter.format()).isEqualTo(ExportFormat.CSV);
    }
}
