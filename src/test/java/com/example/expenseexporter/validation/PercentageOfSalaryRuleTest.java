package com.example.expenseexporter.validation;

import com.example.expenseexporter.domain.Position;
import com.example.expenseexporter.domain.ValidationResult;
import com.example.expenseexporter.domain.ValidationResult.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static com.example.expenseexporter.validation.RuleFixtures.employee;
import static com.example.expenseexporter.validation.RuleFixtures.invoice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PercentageOfSalaryRuleTest {

    @ParameterizedTest
    @CsvSource({
            "5,   3333.33, 166.67",   // 166.6665 rounds half-up
            "5,   4200.00, 210.00",
            "10,  6000.00, 600.00",
            "7.5, 2000.00, 150.00",
            "0,   5000.00, 0.00",
            "100, 1234.56, 1234.56",
            "5,   0.00,    0.00"})
    void limitIsPercentOfSalaryRoundedToCents(String percent, String salary, String expectedLimit) {
        PercentageOfSalaryRule rule = new PercentageOfSalaryRule(new BigDecimal(percent));

        assertThat(rule.limitFor(employee(Position.SENIOR, salary))).isEqualTo(new BigDecimal(expectedLimit));
    }

    @Test
    void approvesUpToTheLimitInclusive() {
        PercentageOfSalaryRule rule = new PercentageOfSalaryRule(new BigDecimal("5"));

        assertThat(rule.validate(invoice("166.67"), employee(Position.SENIOR, "3333.33")).status())
                .isEqualTo(Status.APPROVED);
        assertThat(rule.validate(invoice("166.68"), employee(Position.SENIOR, "3333.33")).status())
                .isEqualTo(Status.REJECTED);
    }

    @Test
    void zeroSalaryRejectsEveryInvoice() {
        ValidationResult result = new PercentageOfSalaryRule(new BigDecimal("10"))
                .validate(invoice("0.01"), employee(Position.MANAGER, "0.00"));

        assertThat(result.status()).isEqualTo(Status.REJECTED);
        assertThat(result.limit()).isEqualTo(new BigDecimal("0.00"));
    }

    @Test
    void describesPercentWithoutTrailingZeros() {
        assertThat(new PercentageOfSalaryRule(new BigDecimal("5.00")).describe()).isEqualTo("5% of monthly salary");
        assertThat(new PercentageOfSalaryRule(new BigDecimal("7.5")).describe()).isEqualTo("7.5% of monthly salary");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.01", "100.01", "250"})
    void rejectsPercentOutsideZeroToHundred(String percent) {
        assertThatExceptionOfType(InvalidRuleException.class)
                .isThrownBy(() -> new PercentageOfSalaryRule(new BigDecimal(percent)))
                .withMessageContaining("between 0 and 100");
    }

    @Test
    void rejectsMissingPercent() {
        assertThatExceptionOfType(InvalidRuleException.class).isThrownBy(() -> new PercentageOfSalaryRule(null));
    }
}
