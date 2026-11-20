package com.example.expenseexporter.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void listsAndFindsEmployees() throws Exception {
        mvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
        mvc.perform(get("/api/employees/E-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Anna Müller"))
                .andExpect(jsonPath("$.position").value("INTERN"))
                .andExpect(jsonPath("$.salaryCurrency").value("EUR"));
        mvc.perform(get("/api/employees/E-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Employee E-999 not found"));
    }

    @Test
    @DirtiesContext
    void addsAValidEmployee() throws Exception {
        mvc.perform(postEmployee("E-100", "MANAGER", "5000.00"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/employees/E-100"));

        mvc.perform(get("/api/employees/E-100")).andExpect(jsonPath("$.position").value("MANAGER"));
    }

    @Test
    void invalidEmployeeIsRejected() throws Exception {
        mvc.perform(postEmployee("E-100", "MANAGER", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("monthlySalary must not be negative: -1"));
        mvc.perform(postEmployee("E-100", "CEO", "1000.00"))
                .andExpect(status().isBadRequest());
        mvc.perform(postEmployee("E-001", "INTERN", "1000.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Employee E-001 already exists"));
    }

    private static org.springframework.test.web.servlet.RequestBuilder postEmployee(
            String id, String position, String salary) {
        String json = """
                {"id": "%s", "fullName": "Test Person", "position": "%s",
                 "monthlySalary": %s, "salaryCurrency": "EUR"}
                """.formatted(id, position, salary);
        return post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json);
    }
}
