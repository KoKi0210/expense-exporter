package com.example.expenseexporter.export;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ExportFormatTest {

    @Test
    void eachFormatCreatesItsExporter() {
        assertThat(ExportFormat.CSV.createExporter()).isInstanceOf(CsvExporter.class);
        assertThat(ExportFormat.JSON.createExporter()).isInstanceOf(JsonExporter.class);
        assertThat(ExportFormat.XML.createExporter()).isInstanceOf(XmlExporter.class);
    }

    @ParameterizedTest
    @EnumSource(ExportFormat.class)
    void exporterReportsTheFormatThatCreatedIt(ExportFormat format) {
        assertThat(format.createExporter().format()).isEqualTo(format);
    }
}
