package com.example.expenseexporter.export;

import com.example.expenseexporter.domain.ValidatedExpense;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.expenseexporter.export.ExportFixtures.approvedEur;
import static com.example.expenseexporter.export.ExportFixtures.rejectedUsd;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class InvoiceExporterTemplateTest {

    /** Records every step the template method calls. */
    private static final class RecordingExporter extends InvoiceExporter {
        final List<String> calls = new ArrayList<>();
        ExportSummary summary;

        @Override
        public ExportFormat format() {
            return ExportFormat.CSV;
        }

        @Override
        protected void writeHeader(StringBuilder out) {
            calls.add("header");
            out.append('[');
        }

        @Override
        protected void writeSeparator(StringBuilder out) {
            calls.add("separator");
            out.append('|');
        }

        @Override
        protected void writeRecord(StringBuilder out, ValidatedExpense expense) {
            calls.add("record");
            out.append(expense.invoice().number());
        }

        @Override
        protected void writeFooter(StringBuilder out, ExportSummary summary) {
            calls.add("footer");
            this.summary = summary;
            out.append(']');
        }
    }

    @Test
    void stepsRunInFixedOrderWithSeparatorOnlyBetweenRecords() {
        RecordingExporter exporter = new RecordingExporter();

        String output = exporter.export(List.of(approvedEur(), rejectedUsd()));

        assertThat(exporter.calls).containsExactly("header", "record", "separator", "record", "footer");
        assertThat(output).isEqualTo("[INV-0001|INV-0002]");
    }

    @Test
    void singleRecordHasNoSeparator() {
        RecordingExporter exporter = new RecordingExporter();

        exporter.export(List.of(approvedEur()));

        assertThat(exporter.calls).containsExactly("header", "record", "footer");
    }

    @Test
    void emptyListStillWritesHeaderAndFooter() {
        RecordingExporter exporter = new RecordingExporter();

        String output = exporter.export(List.of());

        assertThat(exporter.calls).containsExactly("header", "footer");
        assertThat(output).isEqualTo("[]");
    }

    @Test
    void footerReceivesSummaryOfAllRows() {
        RecordingExporter exporter = new RecordingExporter();

        exporter.export(List.of(approvedEur(), rejectedUsd()));

        assertThat(exporter.summary.count()).isEqualTo(2);
        assertThat(exporter.summary.approved()).isEqualTo(1);
        assertThat(exporter.summary.rejected()).isEqualTo(1);
    }

    @Test
    void hooksAreOptional() {
        InvoiceExporter onlyRecords = new InvoiceExporter() {
            @Override
            public ExportFormat format() {
                return ExportFormat.CSV;
            }

            @Override
            protected void writeRecord(StringBuilder out, ValidatedExpense expense) {
                out.append(expense.invoice().number());
            }
        };

        assertThat(onlyRecords.export(List.of(approvedEur(), rejectedUsd()))).isEqualTo("INV-0001INV-0002");
    }

    @Test
    void templateMethodIsFinal() throws NoSuchMethodException {
        int modifiers = InvoiceExporter.class.getMethod("export", List.class).getModifiers();

        assertThat(Modifier.isFinal(modifiers)).isTrue();
    }

    @Test
    void rejectsNullRows() {
        RecordingExporter exporter = new RecordingExporter();

        assertThatNullPointerException().isThrownBy(() -> exporter.export(null));
        assertThatNullPointerException().isThrownBy(() -> exporter.export(Arrays.asList(approvedEur(), null)));
        assertThat(exporter.calls).isEmpty();
    }
}
