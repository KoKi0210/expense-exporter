package com.example.expenseexporter.web;

import com.example.expenseexporter.domain.Position;
import com.example.expenseexporter.validation.RuleDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("expense")
public record ExpenseRulesProperties(Map<Position, RuleDefinition> rules) {

  public ExpenseRulesProperties {
    rules = rules == null ? Map.of() : rules;
  }
}
