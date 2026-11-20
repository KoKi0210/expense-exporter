package com.example.expenseexporter.validation;

import com.example.expenseexporter.domain.Employee;
import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.Position;
import com.example.expenseexporter.domain.ValidatedExpense;
import com.example.expenseexporter.domain.ValidationResult;
import com.example.expenseexporter.domain.ValidationResult.Status;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static com.example.expenseexporter.validation.RuleFixtures.USD;
import static com.example.expenseexporter.validation.RuleFixtures.employee;
import static com.example.expenseexporter.validation.RuleFixtures.invoice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ExpenseValidatorTest {

    /** A rule that reports its own name, so tests can see which rule was picked. */
    private record NamedRule(String name) implements ValidationRule {
        @Override
        public ValidationResult validate(Invoice invoice, Employee employee) {
            return new ValidationResult(Status.APPROVED, BigDecimal.ZERO, name, null);
        }

        @Override
        public String describe() {
            return name;
        }
    }

    private static Map<Position, ValidationRule> oneRulePerPosition() {
        Map<Position, ValidationRule> rules = new EnumMap<>(Position.class);
        for (Position position : Position.values()) {
            rules.put(position, new NamedRule(position.name()));
        }
        return rules;
    }

    @Test
    void delegatesToTheRuleOfTheEmployeesPosition() {
        ExpenseValidator validator = new ExpenseValidator(oneRulePerPosition());

        for (Position position : Position.values()) {
            ValidatedExpense row = validator.validate(invoice("10.00"), employee(position, "1000.00"));

            assertThat(row.result().ruleDescription()).isEqualTo(position.name());
        }
    }

    @Test
    void returnsInvoiceAndEmployeeWithTheResult() {
        ExpenseValidator validator = new ExpenseValidator(oneRulePerPosition());
        Invoice invoice = invoice("10.00");
        Employee employee = employee(Position.INTERN, "1000.00");

        ValidatedExpense row = validator.validate(invoice, employee);

        assertThat(row.invoice()).isSameAs(invoice);
        assertThat(row.employee()).isSameAs(employee);
    }

    @Test
    void replacedRuleIsUsedFromTheNextValidation() {
        ExpenseValidator validator = new ExpenseValidator(oneRulePerPosition());
        Employee manager = employee(Position.MANAGER, "6000.00");
        assertThat(validator.validate(invoice("700.00"), manager).result().status()).isEqualTo(Status.APPROVED);

        validator.replaceRule(Position.MANAGER, new PercentageOfSalaryRule(new BigDecimal("10")));

        ValidationResult result = validator.validate(invoice("700.00"), manager).result();
        assertThat(result.status()).isEqualTo(Status.REJECTED);
        assertThat(result.limit()).isEqualTo(new BigDecimal("600.00"));
        assertThat(validator.ruleFor(Position.INTERN)).isEqualTo(new NamedRule("INTERN"));
    }

    @Test
    void currencyMismatchIsAnErrorAndTheRuleIsNotCalled() {
        ExpenseValidator validator = new ExpenseValidator(oneRulePerPosition());

        ValidationResult result = validator.validate(invoice("10.00", USD), employee(Position.JUNIOR, "2400.00"))
                .result();

        assertThat(result.status()).isEqualTo(Status.ERROR);
        assertThat(result.limit()).isNull();
        assertThat(result.ruleDescription()).isEqualTo("JUNIOR");
        assertThat(result.message()).isEqualTo("Invoice currency USD differs from salary currency EUR");
    }

    @Test
    void failsFastWhenAPositionHasNoRule() {
        Map<Position, ValidationRule> rules = oneRulePerPosition();
        rules.remove(Position.SENIOR);
        rules.remove(Position.DIRECTOR);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ExpenseValidator(rules))
                .withMessage("No validation rule for position(s): [SENIOR, DIRECTOR]");
    }

    @Test
    void laterChangesToTheConstructorMapDoNotAffectTheValidator() {
        Map<Position, ValidationRule> rules = new HashMap<>(oneRulePerPosition());
        ExpenseValidator validator = new ExpenseValidator(rules);

        rules.put(Position.INTERN, new NamedRule("changed"));

        assertThat(validator.ruleFor(Position.INTERN)).isEqualTo(new NamedRule("INTERN"));
    }

    @Test
    void rulesSnapshotIsOrderedAndReadOnly() {
        ExpenseValidator validator = new ExpenseValidator(oneRulePerPosition());

        assertThat(validator.rules().keySet()).containsExactly(Position.values());
        assertThat(validator.rules()).isUnmodifiable();
    }

    @Test
    void rejectsNullReplacement() {
        ExpenseValidator validator = new ExpenseValidator(oneRulePerPosition());

        assertThatNullPointerException().isThrownBy(() -> validator.replaceRule(Position.INTERN, null));
    }
}
