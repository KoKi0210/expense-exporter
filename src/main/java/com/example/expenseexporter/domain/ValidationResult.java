package com.example.expenseexporter.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record ValidationResult(
        Status status,
        BigDecimal limit,
        String ruleDescription,
        String message) {

    public enum Status {
        APPROVED,
        REJECTED,
        ERROR
    }

    public ValidationResult {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(ruleDescription, "ruleDescription");
    }
}
