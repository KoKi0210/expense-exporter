package com.example.expenseexporter.formula;

import java.math.BigDecimal;
import java.math.MathContext;

final class FormulaParser {

  private static final int END = -1;

  private final String source;
  private final BigDecimal salary;
  private int pos;
  private int nesting;

  private FormulaParser(String source, BigDecimal salary) {
    this.source = source;
    this.salary = salary;
  }

  static void check(String source) {
    run(source, null);
  }

  static BigDecimal calculate(String source, BigDecimal salary) {
    return run(source, salary);
  }

  private static BigDecimal run(String source, BigDecimal salary) {
    if (source == null || source.isBlank()) {
      throw new FormulaSyntaxException("Formula must not be empty");
    }
    if (source.length() > Formula.MAX_LENGTH) {
      throw new FormulaSyntaxException("Formula is too long: " + source.length()
          + " characters, the maximum is " + Formula.MAX_LENGTH);
    }
    FormulaParser parser = new FormulaParser(source, salary);
    BigDecimal result = parser.expression();
    if (parser.peek() != END) {
      throw parser.unexpected();
    }
    return result;
  }

  private BigDecimal expression() {
    BigDecimal result = term();
    while (peek() == '+' || peek() == '-') {
      char operator = source.charAt(pos++);
      BigDecimal right = term();
      result = operator == '+' ? result.add(right) : result.subtract(right);
    }
    return result;
  }

  private BigDecimal term() {
    BigDecimal result = unary();
    while (peek() == '*' || peek() == '/') {
      char operator = source.charAt(pos++);
      BigDecimal right = unary();
      result = operator == '*' ? result.multiply(right) : divide(result, right);
    }
    return result;
  }

  private BigDecimal unary() {
    if (peek() != '-') {
      return primary();
    }
    enter(pos++);
    BigDecimal value = unary().negate();
    nesting--;
    return value;
  }

  private BigDecimal primary() {
    int c = peek();
    int start = pos;
    if (isDigit(c)) {
      pos = numberEnd(start);
      return new BigDecimal(source.substring(start, pos));
    }
    if (isLetter(c)) {
      pos = nameEnd(start);
      String name = source.substring(start, pos);
      if (!name.equals(Formula.SALARY)) {
        throw error("Unknown variable '" + name + "' (only '" + Formula.SALARY + "' is allowed)", start);
      }
      return salary == null ? BigDecimal.ONE : salary;
    }
    if (c == '(') {
      enter(pos++);
      BigDecimal inner = expression();
      if (peek() != ')') {
        throw error("Expected ')' but found " + describe(pos), pos);
      }
      pos++;
      nesting--;
      return inner;
    }
    throw unexpected();
  }

  private BigDecimal divide(BigDecimal left, BigDecimal right) {
    if (right.signum() == 0) {
      if (salary == null) {
        return BigDecimal.ZERO;
      }
      throw new FormulaEvaluationException("Division by zero");
    }
    return left.divide(right, MathContext.DECIMAL64);
  }

  private void enter(int at) {
    nesting++;
    if (nesting > Formula.MAX_DEPTH) {
      throw error("Formula is nested too deeply (maximum depth is " + Formula.MAX_DEPTH + ")", at);
    }
  }

  private int peek() {
    while (pos < source.length() && (source.charAt(pos) == ' ' || source.charAt(pos) == '\t')) {
      pos++;
    }
    return pos < source.length() ? source.charAt(pos) : END;
  }

  private int numberEnd(int start) {
    int i = digitsEnd(start);
    if (i < source.length() && source.charAt(i) == '.') {
      int fractionStart = i + 1;
      i = digitsEnd(fractionStart);
      if (i == fractionStart || (i < source.length() && source.charAt(i) == '.')) {
        throw error("Invalid number", start);
      }
    }
    return i;
  }

  private int digitsEnd(int start) {
    int i = start;
    while (i < source.length() && isDigit(source.charAt(i))) {
      i++;
    }
    return i;
  }

  private int nameEnd(int start) {
    int i = start;
    while (i < source.length() && (isLetter(source.charAt(i)) || isDigit(source.charAt(i)))) {
      i++;
    }
    return i;
  }

  private String describe(int at) {
    if (at == source.length()) {
      return "end of formula";
    }
    char c = source.charAt(at);
    int end;
    if (isDigit(c)) {
      end = numberEnd(at);
    } else if (isLetter(c)) {
      end = nameEnd(at);
    } else if ("+-*/()".indexOf(c) >= 0) {
      end = at + 1;
    } else {
      throw error("Unexpected character '" + c + "'", at);
    }
    return "'" + source.substring(at, end) + "'";
  }

  private FormulaSyntaxException unexpected() {
    peek();
    return error("Unexpected " + describe(pos), pos);
  }

  private FormulaSyntaxException error(String problem, int at) {
    return new FormulaSyntaxException(problem, source, at);
  }

  private static boolean isDigit(int c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isLetter(int c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }
}
