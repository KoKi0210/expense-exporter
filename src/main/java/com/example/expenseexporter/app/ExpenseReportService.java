package com.example.expenseexporter.app;

import com.example.expenseexporter.domain.Employee;
import com.example.expenseexporter.domain.ValidatedExpense;
import com.example.expenseexporter.export.ExportFormat;
import com.example.expenseexporter.validation.ExpenseValidator;

import java.util.List;
import java.util.Objects;

public class ExpenseReportService {

    private final EmployeeStore employees;
    private final InvoiceStore invoices;
    private final ExpenseValidator validator;

    public ExpenseReportService(EmployeeStore employees, InvoiceStore invoices, ExpenseValidator validator) {
        this.employees = Objects.requireNonNull(employees, "employees");
        this.invoices = Objects.requireNonNull(invoices, "invoices");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    public List<ValidatedExpense> validateAll() {
        return invoices.findAll().stream()
                .map(invoice -> {
                    Employee employee = employees.findById(invoice.employeeId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "Invoice " + invoice.number() + " refers to unknown employee "
                                            + invoice.employeeId()));
                    return validator.validate(invoice, employee);
                })
                .toList();
    }

    public String export(ExportFormat format) {
        return format.createExporter().export(validateAll());
    }
}
