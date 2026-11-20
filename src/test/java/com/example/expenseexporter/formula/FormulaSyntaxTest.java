package com.example.expenseexporter.formula;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FormulaSyntaxTest {

    @ParameterizedTest(name = "''{0}'' -> {1}")
    @CsvSource(delimiter = '|', value = {
            "2+            | Unexpected end of formula at position 3",
            "(2            | Expected ')' but found end of formula at position 3",
            "2)            | Unexpected ')' at position 2",
            "2 3           | Unexpected '3' at position 3",
            "*2            | Unexpected '*' at position 1",
            "()            | Unexpected ')' at position 2",
            "salary*)      | Unexpected ')' at position 8",
            "2..3          | Invalid number at position 1",
            "2.            | Invalid number at position 1",
            "1.2.3         | Invalid number at position 1",
            ".5            | Unexpected character '.' at position 1",
            "$             | Unexpected character '$' at position 1",
            "salary % 2    | Unexpected character '%' at position 8",
            "sal ary       | Unknown variable 'sal' (only 'salary' is allowed) at position 1",
            "bonus         | Unknown variable 'bonus' (only 'salary' is allowed) at position 1",
            "amount * 2    | Unknown variable 'amount' (only 'salary' is allowed) at position 1",
            "Salary        | Unknown variable 'Salary' (only 'salary' is allowed) at position 1",
            "max(salary)   | Unknown variable 'max' (only 'salary' is allowed) at position 1",
            "salary,1      | Unexpected character ',' at position 7",
            "(2 $          | Unexpected character '$' at position 4",
            "(2 3          | Expected ')' but found '3' at position 4"})
    void reportsTheProblemAndItsPosition(String source, String expectedMessage) {
        assertThatExceptionOfType(FormulaSyntaxException.class)
                .isThrownBy(() -> Formula.parse(source))
                .withMessage(expectedMessage + " in '" + source + "'");
    }

    @Test
    void exposesThePosition() {
        FormulaSyntaxException e = catchSyntaxError("salary*)");

        assertThat(e.position()).isEqualTo(8);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\t"})
    void rejectsEmptyFormula(String source) {
        assertThatExceptionOfType(FormulaSyntaxException.class)
                .isThrownBy(() -> Formula.parse(source))
                .withMessage("Formula must not be empty");
    }

    @Test
    void acceptsFormulaOfMaximumLength() {
        String longButShallow = "salary * 0.12" + " ".repeat(Formula.MAX_LENGTH - 13);

        assertThat(longButShallow).hasSize(Formula.MAX_LENGTH);
        assertThat(Formula.parse(longButShallow).evaluateWithSalary(BigDecimal.TEN)).isEqualByComparingTo("1.2");
    }

    @Test
    void rejectsFormulaLongerThanTheLimit() {
        String source = "salary" + " ".repeat(Formula.MAX_LENGTH - 6) + "+";

        assertThatExceptionOfType(FormulaSyntaxException.class)
                .isThrownBy(() -> Formula.parse(source))
                .withMessage("Formula is too long: 257 characters, the maximum is 256");
    }

    @Test
    void thousandOpenParenthesesAreRejectedNotAStackOverflow() {
        assertThatExceptionOfType(FormulaSyntaxException.class)
                .isThrownBy(() -> Formula.parse("(".repeat(1000) + "1" + ")".repeat(1000)));
    }

    @Test
    void parenthesesUpToTheDepthLimitAreAccepted() {
        int max = Formula.MAX_DEPTH;

        assertThat(Formula.parse("(".repeat(max) + "7" + ")".repeat(max)).evaluateWithSalary(BigDecimal.ONE))
                .isEqualByComparingTo("7");
        assertTooDeep("(".repeat(max + 1) + "7" + ")".repeat(max + 1));
    }

    @Test
    void unaryMinusCountsTowardsTheDepth() {
        int max = Formula.MAX_DEPTH;

        assertThat(Formula.parse("-".repeat(max) + "1").evaluateWithSalary(BigDecimal.ONE))
                .isEqualByComparingTo("1");
        assertTooDeep("-".repeat(max + 1) + "1");
    }

    @Test
    void longFlatChainIsFineBecauseItIsALoopNotRecursion() {
        String source = "1" + "+1".repeat(100);

        assertThat(Formula.parse(source).evaluateWithSalary(BigDecimal.ONE)).isEqualByComparingTo("101");
    }

    @Test
    void divisionByZeroDoesNotHideALaterSyntaxError() {
        assertThatExceptionOfType(FormulaSyntaxException.class)
                .isThrownBy(() -> Formula.parse("1/0 + )"))
                .withMessage("Unexpected ')' at position 7 in '1/0 + )'");
    }

    private static void assertTooDeep(String source) {
        assertThatExceptionOfType(FormulaSyntaxException.class)
                .isThrownBy(() -> Formula.parse(source))
                .withMessageStartingWith("Formula is nested too deeply (maximum depth is 32)");
    }

    private static FormulaSyntaxException catchSyntaxError(String source) {
        try {
            Formula.parse(source);
        } catch (FormulaSyntaxException e) {
            return e;
        }
        throw new AssertionError("Expected a syntax error for " + source);
    }
}
