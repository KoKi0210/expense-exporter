package com.example.expenseexporter.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InvoiceApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void listsAndFindsInvoices() throws Exception {
        mvc.perform(get("/api/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value("INV-0001"));
        mvc.perform(get("/api/invoices/INV-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.issueDate").value("2026-10-02"));
        mvc.perform(get("/api/invoices/INV-9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Invoice INV-9999 not found"));
    }

    @Test
    void validationShowsTheResultOfEachInvoice() throws Exception {
        mvc.perform(get("/api/invoices/validation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoice.number").value("INV-0001"))
                .andExpect(jsonPath("$[0].employee.id").value("E-001"))
                .andExpect(jsonPath("$[0].result.status").value("APPROVED"))
                .andExpect(jsonPath("$[*].result.status").value(hasItems("APPROVED", "REJECTED")));
    }

    @Test
    @DirtiesContext
    void addsAValidInvoice() throws Exception {
        mvc.perform(postInvoice(invoiceJson("INV-0100", "E-001", "12.50", "EUR")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/invoices/INV-0100"))
                .andExpect(jsonPath("$.number").value("INV-0100"));

        mvc.perform(get("/api/invoices/INV-0100")).andExpect(status().isOk());
    }

    @Test
    void invalidDataIsRejectedByTheInvoiceRecord() throws Exception {
        mvc.perform(postInvoice(invoiceJson("INV-0100", "E-001", "-1", "EUR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"))
                .andExpect(jsonPath("$.detail").value("amount must be greater than 0: -1"));
        mvc.perform(postInvoice(invoiceJson("INV-0100", "E-001", "1.005", "EUR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("amount must have at most 2 decimal places: 1.005"));
        mvc.perform(postInvoice(invoiceJson(" ", "E-001", "1.00", "EUR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("number must not be blank"));
        mvc.perform(postInvoice("{\"number\": \"INV-0100\", \"employeeId\": \"E-001\", \"issueDate\": \"2026-11-01\","
                        + " \"description\": \"\", \"amount\": 1, \"currency\": \"EUR\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("vendor is required"));
        mvc.perform(postInvoice(invoiceJson("INV-0100", "E-001", "1.00", "EURO")))
                .andExpect(status().isBadRequest());
        mvc.perform(postInvoice("{\"number\":"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invoiceMustUseTheEmployeesSalaryCurrency() throws Exception {
        mvc.perform(postInvoice(invoiceJson("INV-0100", "E-001", "1.00", "USD")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Invoice currency USD differs from the salary currency EUR of employee E-001"));
    }

    @Test
    void unknownEmployeeIsNotFound() throws Exception {
        mvc.perform(postInvoice(invoiceJson("INV-0100", "E-999", "1.00", "EUR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Employee E-999 not found"));
    }

    @Test
    void duplicateNumberIsRejected() throws Exception {
        mvc.perform(postInvoice(invoiceJson("INV-0001", "E-001", "1.00", "EUR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invoice INV-0001 already exists"));
    }

    private static RequestBuilder postInvoice(String json) {
        return post("/api/invoices").contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private static String invoiceJson(String number, String employeeId, String amount, String currency) {
        return """
                {"number": "%s", "employeeId": "%s", "issueDate": "2026-11-01", "vendor": "Shop",
                 "description": "Pens", "amount": %s, "currency": "%s"}
                """.formatted(number, employeeId, amount, currency);
    }
}
