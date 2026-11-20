package com.example.expenseexporter.app;

import com.example.expenseexporter.domain.Employee;
import com.example.expenseexporter.domain.Invoice;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SampleDataTest {

    @Test
    void everyInvoiceBelongsToAKnownEmployeeAndUsesTheirCurrency() {
        Map<String, Employee> employees = SampleData.employees().stream()
                .collect(Collectors.toMap(Employee::id, Function.identity()));

        for (Invoice invoice : SampleData.invoices()) {
            Employee employee = employees.get(invoice.employeeId());
            assertThat(employee).as("employee of %s", invoice.number()).isNotNull();
            assertThat(invoice.currency()).as("currency of %s", invoice.number())
                    .isEqualTo(employee.salaryCurrency());
        }
    }

    @Test
    void loadsEverythingIntoTheStores() {
        EmployeeStore employees = new EmployeeStore();
        InvoiceStore invoices = new InvoiceStore();

        SampleData.loadInto(employees, invoices);

        assertThat(employees.findAll()).hasSameSizeAs(SampleData.employees());
        assertThat(invoices.findAll()).hasSameSizeAs(SampleData.invoices());
    }
}
