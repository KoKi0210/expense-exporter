package com.example.expenseexporter.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RuleApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void rulesComeFromApplicationYml() throws Exception {
        mvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.INTERN").value("Fixed amount: 100.00"))
                .andExpect(jsonPath("$.SENIOR").value("5% of monthly salary"))
                .andExpect(jsonPath("$.DIRECTOR").value("Formula: salary * 0.12 + 500"));
    }

    @Test
    @DirtiesContext
    void replacedRuleIsUsedForTheNextValidation() throws Exception {
        mvc.perform(putRule("MANAGER", "{\"type\": \"CUSTOM_FORMULA\", \"formula\": \"salary * 0.2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.MANAGER").value("Formula: salary * 0.2"));

        // INV-0005 belongs to the manager Petar Ivanov (salary 6000.00 EUR): limit 6000 * 0.2 = 1200.
        mvc.perform(get("/api/invoices/validation"))
                .andExpect(jsonPath("$[?(@.invoice.number == 'INV-0005')].result.ruleDescription")
                        .value("Formula: salary * 0.2"))
                .andExpect(jsonPath("$[?(@.invoice.number == 'INV-0005')].result.limit").value(1200.0));
    }

    @Test
    void invalidFormulaIsRejectedAndTheOldRuleStays() throws Exception {
        mvc.perform(putRule("MANAGER", "{\"type\": \"CUSTOM_FORMULA\", \"formula\": \"salary *\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid formula"))
                .andExpect(jsonPath("$.detail").value("Unexpected end of formula at position 9 in 'salary *'"));

        mvc.perform(get("/api/rules")).andExpect(jsonPath("$.MANAGER").value("10% of monthly salary"));
    }

    @Test
    void invalidRuleValuesAreRejected() throws Exception {
        mvc.perform(putRule("INTERN", "{\"type\": \"PERCENTAGE_OF_SALARY\", \"percent\": \"150\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid rule"))
                .andExpect(jsonPath("$.detail").value("Percent must be between 0 and 100: 150"));
        mvc.perform(putRule("INTERN", "{\"amount\": \"100.00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Rule type is required"));
        mvc.perform(putRule("INTERN", "{\"type\": \"NOPE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));
    }

    @Test
    void unknownPositionIsABadRequest() throws Exception {
        mvc.perform(putRule("CEO", "{\"type\": \"FIXED_AMOUNT\", \"amount\": \"1.00\"}"))
                .andExpect(status().isBadRequest());
    }

    private static org.springframework.test.web.servlet.RequestBuilder putRule(String position, String json) {
        return put("/api/rules/" + position).contentType(MediaType.APPLICATION_JSON).content(json);
    }
}
