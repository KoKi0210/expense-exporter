package com.example.expenseexporter.validation;

import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.ValidationResult;
import com.example.expenseexporter.domain.ValidationResult.Status;

import java.math.BigDecimal;

final class LimitCheck {

    private LimitCheck() {
    }

    static ValidationResult compare(Invoice invoice, BigDecimal limit, String ruleDescription) {
        if (invoice.amount().compareTo(limit) <= 0) {
            return new ValidationResult(Status.APPROVED, limit, ruleDescription, null);
        }
        String currency = invoice.currency().getCurrencyCode();
        String message = "Amount " + invoice.amount().toPlainString() + " " + currency
                + " exceeds the limit of " + limit.toPlainString() + " " + currency
                + " by " + invoice.amount().subtract(limit).toPlainString();
        return new ValidationResult(Status.REJECTED, limit, ruleDescription, message);
    }
}
