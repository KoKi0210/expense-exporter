package com.example.expenseexporter.web;

import com.example.expenseexporter.domain.Position;
import com.example.expenseexporter.validation.ExpenseValidator;
import com.example.expenseexporter.validation.RuleDefinition;
import com.example.expenseexporter.validation.ValidationRule;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

  private final ExpenseValidator validator;

  public RuleController(ExpenseValidator validator) {
    this.validator = validator;
  }

  @GetMapping
  public Map<Position, String> rules() {
    Map<Position, String> descriptions = new LinkedHashMap<>();
    validator.rules().forEach((position, rule) -> descriptions.put(position, rule.describe()));
    return descriptions;
  }

  @PutMapping("/{position}")
  public Map<Position, String> replaceRule(@PathVariable Position position, @RequestBody RuleDefinition definition) {
    ValidationRule rule = definition.toRule();
    validator.replaceRule(position, rule);
    return Map.of(position, rule.describe());
  }
}
