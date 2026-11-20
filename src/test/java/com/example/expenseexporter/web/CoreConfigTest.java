package com.example.expenseexporter.web;

import com.example.expenseexporter.formula.FormulaSyntaxException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/** Checks that invalid rule configuration stops the application from starting. */
class CoreConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(CoreConfig.class);

    @Test
    void startsWithARuleForEveryPosition() {
        runner.withPropertyValues(
                        "expense.rules.INTERN.type=FIXED_AMOUNT", "expense.rules.INTERN.amount=100.00",
                        "expense.rules.JUNIOR.type=FIXED_AMOUNT", "expense.rules.JUNIOR.amount=300.00",
                        "expense.rules.SENIOR.type=PERCENTAGE_OF_SALARY", "expense.rules.SENIOR.percent=5",
                        "expense.rules.MANAGER.type=PERCENTAGE_OF_SALARY", "expense.rules.MANAGER.percent=10",
                        "expense.rules.DIRECTOR.type=CUSTOM_FORMULA", "expense.rules.DIRECTOR.formula=salary * 0.12")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void missingPositionStopsStartup() {
        runner.withPropertyValues("expense.rules.INTERN.type=FIXED_AMOUNT", "expense.rules.INTERN.amount=100.00")
                .run(context -> assertThat(context).getFailure()
                        .rootCause()
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("No validation rule for position(s): [JUNIOR, SENIOR, MANAGER, DIRECTOR]"));
    }

    @Test
    void invalidFormulaStopsStartup() {
        runner.withPropertyValues("expense.rules.DIRECTOR.type=CUSTOM_FORMULA", "expense.rules.DIRECTOR.formula=salary *")
                .run(context -> assertThat(context).getFailure()
                        .rootCause()
                        .isInstanceOf(FormulaSyntaxException.class));
    }
}
