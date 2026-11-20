package com.example.expenseexporter.validation;

import com.example.expenseexporter.domain.Employee;
import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.ValidationResult;
import com.example.expenseexporter.domain.ValidationResult.Status;
import com.example.expenseexporter.formula.Formula;
import com.example.expenseexporter.formula.FormulaEvaluationException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Concrete strategy: the limit is computed by a formula over {@code salary}
 */
public record CustomFormulaRule(Formula formula) implements ValidationRule {

    public CustomFormulaRule {
        if (formula == null) {
            throw new InvalidRuleException("Custom formula rule needs a formula");
        }
    }

    public CustomFormulaRule(String source) {
        this(Formula.parse(source));
    }

    @Override
    public ValidationResult validate(Invoice invoice, Employee employee) {
        BigDecimal limit;
        try {
            limit = limitFor(employee);
        } catch (FormulaEvaluationException e) {
            return new ValidationResult(Status.ERROR, null, describe(),
                    "Formula could not be evaluated: " + e.getMessage());
        }
        return LimitCheck.compare(invoice, limit, describe());
    }

    public BigDecimal limitFor(Employee employee) {
        return formula.evaluateWithSalary(employee.monthlySalary()).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String describe() {
        return "Formula: " + formula.source();
    }
}
