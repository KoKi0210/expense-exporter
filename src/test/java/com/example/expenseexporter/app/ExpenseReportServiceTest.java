package com.example.expenseexporter.app;

import com.example.expenseexporter.domain.Employee;
import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.Position;
import com.example.expenseexporter.domain.ValidatedExpense;
import com.example.expenseexporter.domain.ValidationResult.Status;
import com.example.expenseexporter.export.ExportFormat;
import com.example.expenseexporter.validation.ExpenseValidator;
import com.example.expenseexporter.validation.FixedAmountRule;
import com.example.expenseexporter.validation.ValidationRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class ExpenseReportServiceTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    private final EmployeeStore employees = new EmployeeStore();
    private final InvoiceStore invoices = new InvoiceStore();
    private final ExpenseReportService service =
            new ExpenseReportService(employees, invoices, new ExpenseValidator(fixedLimitForAll("100.00")));

    private static Map<Position, ValidationRule> fixedLimitForAll(String limit) {
        Map<Position, ValidationRule> rules = new EnumMap<>(Position.class);
        for (Position position : Position.values()) {
            rules.put(position, new FixedAmountRule(new BigDecimal(limit)));
        }
        return rules;
    }

    private static Invoice invoice(String number, String employeeId, String amount) {
        return new Invoice(number, employeeId, LocalDate.of(2026, 10, 2), "Vendor", "", new BigDecimal(amount), EUR);
    }

    @Test
    void validatesEveryInvoiceOrderedByNumber() {
        employees.add(new Employee("E-001", "Anna", Position.INTERN, new BigDecimal("1500.00"), EUR));
        invoices.add(invoice("INV-0002", "E-001", "150.00"));
        invoices.add(invoice("INV-0001", "E-001", "50.00"));

        List<ValidatedExpense> rows = service.validateAll();

        assertThat(rows).extracting(row -> row.invoice().number()).containsExactly("INV-0001", "INV-0002");
        assertThat(rows).extracting(row -> row.result().status()).containsExactly(Status.APPROVED, Status.REJECTED);
    }

    @Test
    void exportUsesTheGivenExporter() {
        employees.add(new Employee("E-001", "Anna", Position.INTERN, new BigDecimal("1500.00"), EUR));
        invoices.add(invoice("INV-0001", "E-001", "50.00"));

        String csv = service.export(ExportFormat.CSV);

        assertThat(csv).startsWith("invoiceNumber,").contains("INV-0001,").contains(",APPROVED,");
    }

    @Test
    void unknownEmployeeIsReported() {
        invoices.add(invoice("INV-0001", "E-404", "50.00"));

        assertThatIllegalStateException().isThrownBy(service::validateAll)
                .withMessage("Invoice INV-0001 refers to unknown employee E-404");
    }

    @Test
    void storesRejectDuplicates() {
        Employee anna = new Employee("E-001", "Anna", Position.INTERN, new BigDecimal("1500.00"), EUR);
        employees.add(anna);
        invoices.add(invoice("INV-0001", "E-001", "50.00"));

        assertThatIllegalArgumentException().isThrownBy(() -> employees.add(anna));
        assertThatIllegalArgumentException().isThrownBy(() -> invoices.add(invoice("INV-0001", "E-001", "1.00")));
        assertThat(employees.findById("E-001")).contains(anna);
        assertThat(invoices.findByNumber("INV-0002")).isEmpty();
    }
}
