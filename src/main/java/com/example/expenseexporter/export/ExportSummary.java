package com.example.expenseexporter.export;

import com.example.expenseexporter.domain.ValidatedExpense;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public record ExportSummary(
        int count,
        int approved,
        int rejected,
        int errors,
        SortedMap<Currency, BigDecimal> totalsByCurrency) {

    private static final Comparator<Currency> BY_CODE = Comparator.comparing(Currency::getCurrencyCode);

    public ExportSummary {
        if (approved < 0 || rejected < 0 || errors < 0 || approved + rejected + errors != count) {
            throw new IllegalArgumentException("status counts must be non-negative and add up to count");
        }
        Objects.requireNonNull(totalsByCurrency, "totalsByCurrency");
        SortedMap<Currency, BigDecimal> sorted = new TreeMap<>(BY_CODE);
        sorted.putAll(totalsByCurrency);
        totalsByCurrency = Collections.unmodifiableSortedMap(sorted);
    }

     static ExportSummary of(List<ValidatedExpense> expenses) {
        int approved = 0;
        int rejected = 0;
        int errors = 0;
        SortedMap<Currency, BigDecimal> totals = new TreeMap<>(BY_CODE);
        for (ValidatedExpense expense : expenses) {
            switch (expense.result().status()) {
                case APPROVED -> approved++;
                case REJECTED -> rejected++;
                case ERROR -> errors++;
            }
            totals.merge(expense.invoice().currency(), expense.invoice().amount(), BigDecimal::add);
        }
        return new ExportSummary(expenses.size(), approved, rejected, errors, totals);
    }
}
