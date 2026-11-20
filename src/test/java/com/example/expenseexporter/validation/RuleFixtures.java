package com.example.expenseexporter.validation;

import com.example.expenseexporter.domain.Employee;
import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.Position;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

final class RuleFixtures {

    static final Currency EUR = Currency.getInstance("EUR");
    static final Currency USD = Currency.getInstance("USD");

    private RuleFixtures() {
    }

    static Employee employee(Position position, String salary) {
        return new Employee("E-001", "Test Employee", position, new BigDecimal(salary), EUR);
    }

    static Invoice invoice(String amount) {
        return invoice(amount, EUR);
    }

    static Invoice invoice(String amount, Currency currency) {
        return new Invoice("INV-0001", "E-001", LocalDate.of(2026, 10, 2),
                "Vendor", "Description", new BigDecimal(amount), currency);
    }
}
