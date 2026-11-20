package com.example.expenseexporter.validation;

public class InvalidRuleException extends RuntimeException {

  public InvalidRuleException(String message) {
    super(message);
  }
}
