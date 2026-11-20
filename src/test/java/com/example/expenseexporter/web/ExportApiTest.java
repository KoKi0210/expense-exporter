package com.example.expenseexporter.web;

import com.example.expenseexporter.app.ExpenseReportService;
import com.example.expenseexporter.export.ExportFormat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExportApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ExpenseReportService service;

    @Test
    void csvExport() throws Exception {
        mvc.perform(get("/api/exports/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(content().string(startsWith("invoiceNumber,issueDate,employeeId,")));
    }

    @Test
    void jsonExportIsTheExporterOutputUnchanged() throws Exception {
        String expected = ExportFormat.JSON.createExporter().export(service.validateAll());

        // Equal to the exporter's text, so Jackson did not serialize it again (it would start with a quote).
        mvc.perform(get("/api/exports/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string(expected))
                .andExpect(content().string(startsWith("{")));
    }

    @Test
    void xmlExport() throws Exception {
        mvc.perform(get("/api/exports/xml"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/xml;charset=UTF-8"))
                .andExpect(content().string(startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")));
    }

    @Test
    void formatIsCaseInsensitive() throws Exception {
        mvc.perform(get("/api/exports/CSV")).andExpect(status().isOk());
    }

    @Test
    void unknownFormatIsABadRequest() throws Exception {
        mvc.perform(get("/api/exports/pdf"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.detail").value("Unknown export format 'pdf', expected csv, json or xml"));
    }
}
