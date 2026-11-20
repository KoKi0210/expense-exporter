package com.example.expenseexporter.validation;

import com.example.expenseexporter.domain.Employee;
import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.ValidationResult;

import java.math.BigDecimal;

/**
 * Concrete strategy: the limit is the same fixed amount for everyone, in the employee's own
 *
 * @param maxAmount the limit, zero or positive, at most two decimal places
 */
public record FixedAmountRule(BigDecimal maxAmount) implements ValidationRule {

    public FixedAmountRule {
        if (maxAmount == null) {
            throw new InvalidRuleException("Fixed amount rule needs an amount");
        }
        if (maxAmount.signum() < 0) {
            throw new InvalidRuleException("Fixed amount must not be negative: " + maxAmount.toPlainString());
        }
        if (maxAmount.scale() > 2) {
            throw new InvalidRuleException(
                    "Fixed amount must have at most 2 decimal places: " + maxAmount.toPlainString());
        }
    }

    @Override
    public ValidationResult validate(Invoice invoice, Employee employee) {
        return LimitCheck.compare(invoice, maxAmount, describe());
    }

    @Override
    public String describe() {
        return "Fixed amount: " + maxAmount.toPlainString();
    }
}
