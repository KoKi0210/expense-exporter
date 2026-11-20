package com.example.expenseexporter.validation;

import java.math.BigDecimal;

public record RuleDefinition(RuleType type, BigDecimal amount, BigDecimal percent, String formula) {

  public static RuleDefinition fixedAmount(String amount) {
    return new RuleDefinition(RuleType.FIXED_AMOUNT, new BigDecimal(amount), null, null);
  }

  public static RuleDefinition percentageOfSalary(String percent) {
    return new RuleDefinition(RuleType.PERCENTAGE_OF_SALARY, null, new BigDecimal(percent), null);
  }

  public static RuleDefinition customFormula(String formula) {
    return new RuleDefinition(RuleType.CUSTOM_FORMULA, null, null, formula);
  }

  public ValidationRule toRule() {
    if (type == null) {
      throw new InvalidRuleException("Rule type is required");
    }
    return type.createRule(this);
  }
}
