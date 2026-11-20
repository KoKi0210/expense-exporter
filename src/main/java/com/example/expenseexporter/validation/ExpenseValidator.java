package com.example.expenseexporter.validation;

import com.example.expenseexporter.domain.*;
import com.example.expenseexporter.domain.ValidationResult.Status;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Strategy context: validates each invoice with the rule assigned to its employee's position.
 *
 * <p>The validator does not know how any rule works; it only looks the rule up and delegates. Rules
 * can be swapped at runtime with {@link #replaceRule}. The map is a {@link ConcurrentHashMap}, so a
 * rule can be replaced while another thread is validating.
 */
public class ExpenseValidator {

  private final Map<Position, ValidationRule> rules;

  public ExpenseValidator(Map<Position, ValidationRule> rules) {
    Objects.requireNonNull(rules, "rules");
    List<Position> missing = Arrays.stream(Position.values())
        .filter(position -> rules.get(position) == null)
        .toList();
    if (!missing.isEmpty()) {
      throw new IllegalArgumentException("No validation rule for position(s): " + missing);
    }
    this.rules = new ConcurrentHashMap<>(rules);
  }

  public ValidatedExpense validate(Invoice invoice, Employee employee) {
    ValidationRule rule = ruleFor(employee.position());
    ValidationResult result;
    if (!invoice.currency().equals(employee.salaryCurrency())) {
      result = new ValidationResult(Status.ERROR, null, rule.describe(),
          "Invoice currency " + invoice.currency().getCurrencyCode()
              + " differs from salary currency " + employee.salaryCurrency().getCurrencyCode());
    } else {
      result = rule.validate(invoice, employee);
    }
    return new ValidatedExpense(invoice, employee, result);
  }

  public void replaceRule(Position position, ValidationRule rule) {
    Objects.requireNonNull(position, "position");
    Objects.requireNonNull(rule, "rule");
    rules.put(position, rule);
  }

  public ValidationRule ruleFor(Position position) {
    return rules.get(position);
  }

  public Map<Position, ValidationRule> rules() {
    return Collections.unmodifiableMap(new EnumMap<>(rules));
  }
}
