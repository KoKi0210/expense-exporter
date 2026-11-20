package com.example.expenseexporter.validation;

import com.example.expenseexporter.domain.Employee;
import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.ValidationResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Concrete strategy: the limit is a percentage of the employee's monthly salary, rounded half-up to
 * two decimal places.
 *
 * @param percent between 0 and 100 inclusive, e.g. {@code 5} for 5%
 */
public record PercentageOfSalaryRule(BigDecimal percent) implements ValidationRule {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public PercentageOfSalaryRule {
        if (percent == null) {
            throw new InvalidRuleException("Percentage rule needs a percent");
        }
        if (percent.signum() < 0 || percent.compareTo(HUNDRED) > 0) {
            throw new InvalidRuleException("Percent must be between 0 and 100: " + percent.toPlainString());
        }
    }

    @Override
    public ValidationResult validate(Invoice invoice, Employee employee) {
        return LimitCheck.compare(invoice, limitFor(employee), describe());
    }

    public BigDecimal limitFor(Employee employee) {
        return employee.monthlySalary()
                .multiply(percent)
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    @Override
    public String describe() {
        return percent.stripTrailingZeros().toPlainString() + "% of monthly salary";
    }
}
