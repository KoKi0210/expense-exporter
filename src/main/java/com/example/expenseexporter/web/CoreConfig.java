package com.example.expenseexporter.web;

import com.example.expenseexporter.app.EmployeeStore;
import com.example.expenseexporter.app.ExpenseReportService;
import com.example.expenseexporter.app.InvoiceStore;
import com.example.expenseexporter.app.SampleData;
import com.example.expenseexporter.domain.Position;
import com.example.expenseexporter.validation.ExpenseValidator;
import com.example.expenseexporter.validation.ValidationRule;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(ExpenseRulesProperties.class)
public class CoreConfig {

  @Bean
  EmployeeStore employeeStore() {
    return new EmployeeStore();
  }

  @Bean
  InvoiceStore invoiceStore() {
    return new InvoiceStore();
  }

  @Bean
  ExpenseValidator expenseValidator(ExpenseRulesProperties properties) {
    Map<Position, ValidationRule> rules = new EnumMap<>(Position.class);
    properties.rules().forEach((position, definition) -> rules.put(position, definition.toRule()));
    return new ExpenseValidator(rules);
  }

  @Bean
  ExpenseReportService expenseReportService(EmployeeStore employees, InvoiceStore invoices,
                                            ExpenseValidator validator) {
    return new ExpenseReportService(employees, invoices, validator);
  }

  @Bean
  ApplicationRunner loadSampleData(EmployeeStore employees, InvoiceStore invoices) {
    return args -> SampleData.loadInto(employees, invoices);
  }
}
