package com.example.expenseexporter.app;

import com.example.expenseexporter.domain.Position;
import com.example.expenseexporter.domain.ValidatedExpense;
import com.example.expenseexporter.export.ExportFormat;
import com.example.expenseexporter.formula.Formula;
import com.example.expenseexporter.formula.FormulaEvaluationException;
import com.example.expenseexporter.formula.FormulaSyntaxException;
import com.example.expenseexporter.validation.ExpenseValidator;
import com.example.expenseexporter.validation.RuleDefinition;
import com.example.expenseexporter.validation.ValidationRule;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ConsoleDemo {

    private ConsoleDemo() {
    }

    public static void main(String[] args) throws IOException {
        Path outputDir = Path.of(args.length > 0 ? args[0] : "out");

        EmployeeStore employees = new EmployeeStore();
        InvoiceStore invoices = new InvoiceStore();
        SampleData.loadInto(employees, invoices);
        ExpenseValidator validator = new ExpenseValidator(defaultRules());
        ExpenseReportService service = new ExpenseReportService(employees, invoices, validator);

        System.out.println("=== Rules ===");
        validator.rules().forEach((position, rule) ->
                System.out.printf("%-9s %s%n", position, rule.describe()));

        System.out.println();
        System.out.println("=== Validation ===");
        printTable(service.validateAll());

        System.out.println();
        System.out.println("=== Formulas ===");
        printFormulaChecks();

        System.out.println();
        System.out.println("=== Exports ===");
        Files.createDirectories(outputDir);
        for (ExportFormat format : ExportFormat.values()) {
            Path file = outputDir.resolve("invoices." + format.fileExtension());
            Files.writeString(file, service.export(format), StandardCharsets.UTF_8);
            System.out.println("Wrote " + file.toAbsolutePath().normalize());
        }
    }

    private static Map<Position, ValidationRule> defaultRules() {
        Map<Position, RuleDefinition> definitions = Map.of(
                Position.INTERN, RuleDefinition.fixedAmount("100.00"),
                Position.JUNIOR, RuleDefinition.fixedAmount("300.00"),
                Position.SENIOR, RuleDefinition.percentageOfSalary("5"),
                Position.MANAGER, RuleDefinition.percentageOfSalary("10"),
                Position.DIRECTOR, RuleDefinition.customFormula("salary * 0.12 + 500"));

        Map<Position, ValidationRule> rules = new EnumMap<>(Position.class);
        definitions.forEach((position, definition) -> rules.put(position, definition.toRule()));
        return rules;
    }

    private static void printFormulaChecks() {
        for (String source : List.of("salary * 0.12 + 500", "(salary - 1000) / 3", "1000 / salary")) {
            Formula formula = Formula.parse(source);
            for (String salary : List.of("9000.00", "0.00")) {
                String result;
                try {
                    result = formula.evaluateWithSalary(new BigDecimal(salary))
                            .setScale(2, RoundingMode.HALF_UP).toPlainString();
                } catch (FormulaEvaluationException e) {
                    result = "ERROR: " + e.getMessage();
                }
                System.out.printf("%-20s salary=%-8s -> %s%n", source, salary, result);
            }
        }
        List<String> invalid = List.of(
                "salary *", "bonus * 2", "salary * (0.1", "2..5 + salary", "salary $ 2", "(".repeat(40) + "1" + ")".repeat(40));
        for (String source : invalid) {
            try {
                Formula.parse(source);
                System.out.println("accepted?! " + source);
            } catch (FormulaSyntaxException e) {
                System.out.println("rejected: " + e.getMessage());
            }
        }
    }

    private static void printTable(List<ValidatedExpense> rows) {
        String format = "%-9s %-16s %-9s %10s %10s %-4s %-9s %s%n";
        System.out.printf(format, "INVOICE", "EMPLOYEE", "POSITION", "AMOUNT", "LIMIT", "CUR", "STATUS", "MESSAGE");
        for (ValidatedExpense row : rows) {
            System.out.printf(format,
                    row.invoice().number(),
                    row.employee().fullName(),
                    row.employee().position(),
                    row.invoice().amount().toPlainString(),
                    row.result().limit() == null ? "-" : row.result().limit().toPlainString(),
                    row.invoice().currency().getCurrencyCode(),
                    row.result().status(),
                    row.result().message() == null ? "" : row.result().message());
        }
    }
}
