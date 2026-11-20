package com.example.expenseexporter.validation;

import com.example.expenseexporter.domain.Position;
import com.example.expenseexporter.domain.ValidationResult;
import com.example.expenseexporter.domain.ValidationResult.Status;
import com.example.expenseexporter.formula.Formula;
import com.example.expenseexporter.formula.FormulaSyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static com.example.expenseexporter.validation.RuleFixtures.employee;
import static com.example.expenseexporter.validation.RuleFixtures.invoice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CustomFormulaRuleTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "salary * 0.12 + 500 | 9000.00 | 1580.00",
            "salary * 0.12 + 500 | 0.00    | 500.00",
            "salary / 3          | 1000.00 | 333.33",
            "salary / 3          | 2000.00 | 666.67",
            "250                 | 5000.00 | 250.00"})
    void limitIsTheFormulaRoundedToCents(String source, String salary, String expectedLimit) {
        CustomFormulaRule rule = new CustomFormulaRule(source);

        assertThat(rule.limitFor(employee(Position.DIRECTOR, salary))).isEqualTo(new BigDecimal(expectedLimit));
    }

    @Test
    void approvesUpToTheLimitInclusive() {
        CustomFormulaRule rule = new CustomFormulaRule("salary * 0.12 + 500");

        ValidationResult atLimit = rule.validate(invoice("1580.00"), employee(Position.DIRECTOR, "9000.00"));
        ValidationResult above = rule.validate(invoice("1580.01"), employee(Position.DIRECTOR, "9000.00"));

        assertThat(atLimit.status()).isEqualTo(Status.APPROVED);
        assertThat(above.status()).isEqualTo(Status.REJECTED);
        assertThat(above.message()).isEqualTo("Amount 1580.01 EUR exceeds the limit of 1580.00 EUR by 0.01");
    }

    @Test
    void divisionByZeroGivesAnErrorResultInsteadOfThrowing() {
        CustomFormulaRule rule = new CustomFormulaRule("1000 / salary");

        ValidationResult result = rule.validate(invoice("10.00"), employee(Position.DIRECTOR, "0.00"));

        assertThat(result.status()).isEqualTo(Status.ERROR);
        assertThat(result.limit()).isNull();
        assertThat(result.ruleDescription()).isEqualTo("Formula: 1000 / salary");
        assertThat(result.message()).isEqualTo("Formula could not be evaluated: Division by zero");
    }

    @Test
    void invalidFormulaIsRejectedWhenTheRuleIsCreated() {
        assertThatExceptionOfType(FormulaSyntaxException.class)
                .isThrownBy(() -> new CustomFormulaRule("salary *"));
        assertThatExceptionOfType(InvalidRuleException.class)
                .isThrownBy(() -> new CustomFormulaRule((Formula) null));
    }

    @Test
    void describesTheFormula() {
        assertThat(new CustomFormulaRule("salary * 0.12 + 500").describe()).isEqualTo("Formula: salary * 0.12 + 500");
    }
}
