package com.example.expenseexporter.validation;

/**
 * The kinds of validation rule.
 *
 * <p>Factory Method: {@link #createRule(RuleDefinition)} is abstract and every constant overrides
 * it, so each type decides which {@link ValidationRule} it creates. Missing or invalid values are
 * rejected by the rule constructors themselves. A new constant does not compile until it
 * implements the method.
 */
public enum RuleType {
    FIXED_AMOUNT {
        @Override
        public ValidationRule createRule(RuleDefinition definition) {
            return new FixedAmountRule(definition.amount());
        }
    },
    PERCENTAGE_OF_SALARY {
        @Override
        public ValidationRule createRule(RuleDefinition definition) {
            return new PercentageOfSalaryRule(definition.percent());
        }
    },
    CUSTOM_FORMULA {
        @Override
        public ValidationRule createRule(RuleDefinition definition) {
            return new CustomFormulaRule(definition.formula());
        }
    };

    public abstract ValidationRule createRule(RuleDefinition definition);
}
