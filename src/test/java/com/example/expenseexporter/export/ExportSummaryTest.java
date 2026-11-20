package com.example.expenseexporter.export;

import com.example.expenseexporter.domain.ValidationResult.Status;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.TreeMap;

import static com.example.expenseexporter.export.ExportFixtures.EUR;
import static com.example.expenseexporter.export.ExportFixtures.USD;
import static com.example.expenseexporter.export.ExportFixtures.withAmount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.entry;

class ExportSummaryTest {

    @Test
    void countsStatusesAndKeepsTotalsPerCurrency() {
        ExportSummary summary = ExportSummary.of(List.of(
                withAmount("A", "", "45.50", EUR, Status.APPROVED),
                withAmount("B", "", "210.00", USD, Status.REJECTED),
                withAmount("C", "", "19.99", EUR, Status.ERROR),
                withAmount("D", "", "0.01", USD, Status.APPROVED)));

        assertThat(summary.count()).isEqualTo(4);
        assertThat(summary.approved()).isEqualTo(2);
        assertThat(summary.rejected()).isEqualTo(1);
        assertThat(summary.errors()).isEqualTo(1);
        assertThat(summary.totalsByCurrency()).containsExactly(
                entry(EUR, new BigDecimal("65.49")),
                entry(USD, new BigDecimal("210.01")));
    }

    @Test
    void emptyListHasNoTotals() {
        ExportSummary summary = ExportSummary.of(List.of());

        assertThat(summary.count()).isZero();
        assertThat(summary.totalsByCurrency()).isEmpty();
    }

    @Test
    void totalsAreSortedByCurrencyCodeAndUnmodifiable() {
        TreeMap<Currency, BigDecimal> reversed =
                new TreeMap<>((a, b) -> b.getCurrencyCode().compareTo(a.getCurrencyCode()));
        reversed.put(EUR, BigDecimal.ONE);
        reversed.put(USD, BigDecimal.TEN);

        ExportSummary summary = new ExportSummary(0, 0, 0, 0, reversed);

        assertThat(summary.totalsByCurrency().keySet()).containsExactly(EUR, USD);
        assertThat(summary.totalsByCurrency()).isUnmodifiable();
    }

    @Test
    void rejectsCountsThatDoNotAddUp() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ExportSummary(2, 1, 0, 0, new TreeMap<>()));
    }
}
