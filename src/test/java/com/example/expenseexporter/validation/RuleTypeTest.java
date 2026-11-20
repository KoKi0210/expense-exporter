package com.example.expenseexporter.validation;

import com.example.expenseexporter.formula.FormulaSyntaxException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class RuleTypeTest {

    @Test
    void fixedAmountCreatesFixedAmountRule() {
        ValidationRule rule = RuleDefinition.fixedAmount("100.00").toRule();

        assertThat(rule).isEqualTo(new FixedAmountRule(new BigDecimal("100.00")));
    }

    @Test
    void percentageCreatesPercentageOfSalaryRule() {
        ValidationRule rule = RuleDefinition.percentageOfSalary("5").toRule();

        assertThat(rule).isEqualTo(new PercentageOfSalaryRule(new BigDecimal("5")));
    }

    @Test
    void customFormulaCreatesCustomFormulaRule() {
        ValidationRule rule = RuleType.CUSTOM_FORMULA.createRule(RuleDefinition.customFormula("salary * 0.12 + 500"));

        assertThat(rule).isInstanceOf(CustomFormulaRule.class);
        assertThat(rule.describe()).isEqualTo("Formula: salary * 0.12 + 500");
    }

    @Test
    void missingTypeIsRejected() {
        assertThatExceptionOfType(InvalidRuleException.class)
                .isThrownBy(() -> new RuleDefinition(null, BigDecimal.ONE, null, null).toRule())
                .withMessage("Rule type is required");
    }

    @Test
    void missingValueForTheTypeIsRejected() {
        // Only the field matching the type counts: a percent does not help a FIXED_AMOUNT rule.
        assertThatExceptionOfType(InvalidRuleException.class)
                .isThrownBy(() -> new RuleDefinition(RuleType.FIXED_AMOUNT, null, new BigDecimal("5"), null).toRule())
                .withMessage("Fixed amount rule needs an amount");
        assertThatExceptionOfType(InvalidRuleException.class)
                .isThrownBy(() -> new RuleDefinition(RuleType.PERCENTAGE_OF_SALARY, null, null, null).toRule())
                .withMessage("Percentage rule needs a percent");
        assertThatExceptionOfType(FormulaSyntaxException.class)
                .isThrownBy(() -> new RuleDefinition(RuleType.CUSTOM_FORMULA, null, null, null).toRule())
                .withMessage("Formula must not be empty");
    }

    @Test
    void invalidValuesAreRejected() {
        assertThatExceptionOfType(InvalidRuleException.class)
                .isThrownBy(() -> RuleDefinition.fixedAmount("-1.00").toRule());
        assertThatExceptionOfType(InvalidRuleException.class)
                .isThrownBy(() -> RuleDefinition.percentageOfSalary("101").toRule());
        assertThatExceptionOfType(FormulaSyntaxException.class)
                .isThrownBy(() -> RuleDefinition.customFormula("salary *").toRule());
    }
}
