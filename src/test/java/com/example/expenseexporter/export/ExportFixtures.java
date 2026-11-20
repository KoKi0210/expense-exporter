package com.example.expenseexporter.export;

import com.example.expenseexporter.domain.Employee;
import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.Position;
import com.example.expenseexporter.domain.ValidatedExpense;
import com.example.expenseexporter.domain.ValidationResult;
import com.example.expenseexporter.domain.ValidationResult.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

/**
 * Rows shared by the exporter tests. The golden strings in those tests depend on these exact values.
 */
final class ExportFixtures {

    static final Currency EUR = Currency.getInstance("EUR");
    static final Currency USD = Currency.getInstance("USD");

    static final Employee ANNA =
            new Employee("E-001", "Anna Schmidt", Position.JUNIOR, new BigDecimal("2400.00"), EUR);
    static final Employee MARIA =
            new Employee("E-002", "Maria O'Neil", Position.SENIOR, new BigDecimal("4000.00"), USD);

    private ExportFixtures() {
    }

    /** INV-0001: 45.50 EUR, approved, no message. */
    static ValidatedExpense approvedEur() {
        return new ValidatedExpense(
                new Invoice("INV-0001", "E-001", LocalDate.of(2026, 10, 2),
                        "Office Depot", "Notebooks, pens", new BigDecimal("45.50"), EUR),
                ANNA,
                new ValidationResult(Status.APPROVED, new BigDecimal("300.00"), "Fixed amount: 300.00", null));
    }

    /** INV-0002: 210.00 USD, rejected with a message. */
    static ValidatedExpense rejectedUsd() {
        return new ValidatedExpense(
                new Invoice("INV-0002", "E-002", LocalDate.of(2026, 10, 5),
                        "Smith & Sons", "Taxi \"airport\"", new BigDecimal("210.00"), USD),
                MARIA,
                new ValidationResult(Status.REJECTED, new BigDecimal("200.00"), "5% of salary",
                        "Exceeds limit by 10.00"));
    }

    /** An approved EUR row with the given texts, for escaping round trips. */
    static ValidatedExpense withTexts(String vendor, String description) {
        return withAmount(vendor, description, "10.00", EUR, Status.APPROVED);
    }

    /** A row for ANNA (EUR) or MARIA (USD), depending on the currency. */
    static ValidatedExpense withAmount(String vendor, String description, String amount, Currency currency,
                                       Status status) {
        Employee employee = currency.equals(USD) ? MARIA : ANNA;
        return new ValidatedExpense(
                new Invoice("INV-0099", employee.id(), LocalDate.of(2026, 10, 31),
                        vendor, description, new BigDecimal(amount), currency),
                employee,
                new ValidationResult(status, null, "test rule", null));
    }
}
