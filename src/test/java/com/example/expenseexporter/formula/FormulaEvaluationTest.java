package com.example.expenseexporter.formula;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FormulaEvaluationTest {

    private static BigDecimal evaluate(String source, String salary) {
        return Formula.parse(source).evaluateWithSalary(new BigDecimal(salary));
    }

    @ParameterizedTest(name = "{0} = {2}")
    @CsvSource(delimiter = '|', value = {
            "2+3*4               | 0    | 14",
            "(2+3)*4             | 0    | 20",
            "10-4-3              | 0    | 3",
            "8/4/2               | 0    | 1",
            "2*3/4               | 0    | 1.5",
            "-2*3                | 0    | -6",
            "2*-3                | 0    | -6",
            "--2                 | 0    | 2",
            "-(2+3)              | 0    | -5",
            "1.5*2               | 0    | 3",
            "'  2 +\t3  '        | 0    | 5",
            "salary*0.1          | 2500 | 250",
            "salary * 0.12 + 500 | 9000 | 1580",
            "salary * 0.12 + 500 | 0    | 500",
            "(salary - 1000) / 4 | 9000 | 2000",
            "-salary + salary    | 1234 | 0",
            "0.1 + 0.2           | 0    | 0.3"})
    void evaluatesWithPrecedenceAndLeftAssociativity(String source, String salary, String expected) {
        assertThat(evaluate(source, salary)).isEqualByComparingTo(expected);
    }

    @Test
    void divisionThatNeverEndsIsRoundedTo16Digits() {
        assertThat(evaluate("1/3", "0")).isEqualTo(new BigDecimal("0.3333333333333333"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1/0", "1/(salary-salary)", "1000 / salary", "salary / (2 - 2) + 1"})
    void divisionByZeroFailsAtEvaluation(String source) {
        Formula formula = Formula.parse(source);

        assertThatExceptionOfType(FormulaEvaluationException.class)
                .isThrownBy(() -> formula.evaluateWithSalary(BigDecimal.ZERO))
                .withMessage("Division by zero");
    }

    @Test
    void formulaCanBeEvaluatedRepeatedly() {
        Formula formula = Formula.parse("salary / 2");

        assertThat(formula.evaluateWithSalary(new BigDecimal("10"))).isEqualByComparingTo("5");
        assertThat(formula.evaluateWithSalary(new BigDecimal("20"))).isEqualByComparingTo("10");
    }
}
