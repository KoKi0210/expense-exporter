package com.example.expenseexporter.validation;

import com.example.expenseexporter.domain.Employee;
import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.ValidationResult;

/**
 * Strategy: one way of deciding whether an invoice is within its employee's limit.
 *
 * <p>Implementations are immutable, so one rule can be shared by many threads. The
 * {@link ExpenseValidator} (the context) picks the rule by the employee's position.
 */
public interface ValidationRule {

    ValidationResult validate(Invoice invoice, Employee employee);

    String describe();
}
