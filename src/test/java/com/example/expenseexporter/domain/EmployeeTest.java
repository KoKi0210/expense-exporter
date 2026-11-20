package com.example.expenseexporter.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class EmployeeTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void acceptsZeroSalary() {
        Employee unpaid = new Employee("E-001", "Board Member", Position.DIRECTOR, new BigDecimal("0.00"), EUR);

        assertThat(unpaid.monthlySalary()).isZero();
    }

    @Test
    void rejectsNegativeSalaryAndBlankName() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new Employee("E-001", "Anna", Position.INTERN, new BigDecimal("-0.01"), EUR));
        assertThatIllegalArgumentException().isThrownBy(
                () -> new Employee("E-001", " ", Position.INTERN, BigDecimal.TEN, EUR));
    }

    @Test
    void validatedExpenseRequiresTheInvoicesOwnEmployee() {
        Employee anna = new Employee("E-001", "Anna", Position.INTERN, BigDecimal.TEN, EUR);
        Invoice othersInvoice = new Invoice(
                "INV-0001", "E-002", LocalDate.of(2026, 10, 2), "Vendor", "", BigDecimal.ONE, EUR);
        ValidationResult result = new ValidationResult(ValidationResult.Status.APPROVED, null, "rule", null);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ValidatedExpense(othersInvoice, anna, result))
                .withMessageContaining("belongs to E-002");
    }
}
