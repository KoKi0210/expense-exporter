package com.example.expenseexporter.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

public record Invoice(
        String number,
        String employeeId,
        LocalDate issueDate,
        String vendor,
        String description,
        BigDecimal amount,
        Currency currency) {

    public Invoice {
        requireNonBlank(number, "number");
        requireNonBlank(employeeId, "employeeId");
        Objects.requireNonNull(issueDate, "issueDate is required");
        requireNonBlank(vendor, "vendor");
        Objects.requireNonNull(description, "description is required");
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(currency, "currency is required");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0: " + amount);
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("amount must have at most 2 decimal places: " + amount);
        }
    }

    private static void requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, () -> name + " is required");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
