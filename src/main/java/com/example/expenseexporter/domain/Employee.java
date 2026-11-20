package com.example.expenseexporter.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;


public record Employee(
        String id,
        String fullName,
        Position position,
        BigDecimal monthlySalary,
        Currency salaryCurrency) {

    public Employee {
        requireNonBlank(id, "id");
        requireNonBlank(fullName, "fullName");
        Objects.requireNonNull(position, "position is required");
        Objects.requireNonNull(monthlySalary, "monthlySalary is required");
        Objects.requireNonNull(salaryCurrency, "salaryCurrency is required");
        if (monthlySalary.signum() < 0) {
            throw new IllegalArgumentException("monthlySalary must not be negative: " + monthlySalary);
        }
    }

    private static void requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, () -> name + " is required");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
