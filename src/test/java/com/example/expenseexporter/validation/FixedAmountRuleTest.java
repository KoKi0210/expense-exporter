package com.example.expenseexporter.validation;

import com.example.expenseexporter.domain.Position;
import com.example.expenseexporter.domain.ValidationResult;
import com.example.expenseexporter.domain.ValidationResult.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static com.example.expenseexporter.validation.RuleFixtures.employee;
import static com.example.expenseexporter.validation.RuleFixtures.invoice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FixedAmountRuleTest {

    private final FixedAmountRule rule = new FixedAmountRule(new BigDecimal("300.00"));

    @ParameterizedTest
    @CsvSource({
            "0.01,   APPROVED",
            "299.99, APPROVED",
            "300.00, APPROVED",
            "300.01, REJECTED",
            "5000,   REJECTED"})
    void comparesAmountWithLimitInclusive(String amount, Status expected) {
        ValidationResult result = rule.validate(invoice(amount), employee(Position.JUNIOR, "2400.00"));

        assertThat(result.status()).isEqualTo(expected);
        assertThat(result.limit()).isEqualTo(new BigDecimal("300.00"));
        assertThat(result.ruleDescription()).isEqualTo("Fixed amount: 300.00");
    }

    @Test
    void rejectionExplainsByHowMuch() {
        ValidationResult result = rule.validate(invoice("300.01"), employee(Position.JUNIOR, "2400.00"));

        assertThat(result.message()).isEqualTo("Amount 300.01 EUR exceeds the limit of 300.00 EUR by 0.01");
    }

    @Test
    void approvalHasNoMessage() {
        assertThat(rule.validate(invoice("10.00"), employee(Position.JUNIOR, "2400.00")).message()).isNull();
    }

    @Test
    void doesNotDependOnSalary() {
        ValidationResult result = rule.validate(invoice("250.00"), employee(Position.JUNIOR, "0.00"));

        assertThat(result.status()).isEqualTo(Status.APPROVED);
    }

    @Test
    void rejectsInvalidAmounts() {
        assertThatExceptionOfType(InvalidRuleException.class).isThrownBy(() -> new FixedAmountRule(null));
        assertThatExceptionOfType(InvalidRuleException.class)
                .isThrownBy(() -> new FixedAmountRule(new BigDecimal("-1.00")))
                .withMessageContaining("negative");
        assertThatExceptionOfType(InvalidRuleException.class)
                .isThrownBy(() -> new FixedAmountRule(new BigDecimal("1.001")));
    }
}
