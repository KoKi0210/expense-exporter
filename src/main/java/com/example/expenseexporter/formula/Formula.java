package com.example.expenseexporter.formula;

import java.math.BigDecimal;

public record Formula(String source) {

  public static final int MAX_LENGTH = 256;

  public static final int MAX_DEPTH = 32;

  public static final String SALARY = "salary";

  public Formula {
    FormulaParser.check(source);
  }

  public static Formula parse(String source) {
    return new Formula(source);
  }

  public BigDecimal evaluateWithSalary(BigDecimal salary) {
    return FormulaParser.calculate(source, salary);
  }
}
