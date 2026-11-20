package com.example.expenseexporter.domain;

import java.util.Objects;

public record ValidatedExpense(Invoice invoice, Employee employee, ValidationResult result) {

    public ValidatedExpense {
        Objects.requireNonNull(invoice, "invoice");
        Objects.requireNonNull(employee, "employee");
        Objects.requireNonNull(result, "result");
        if (!invoice.employeeId().equals(employee.id())) {
            throw new IllegalArgumentException(
                    "invoice " + invoice.number() + " belongs to " + invoice.employeeId()
                            + ", not to " + employee.id());
        }
    }
}
