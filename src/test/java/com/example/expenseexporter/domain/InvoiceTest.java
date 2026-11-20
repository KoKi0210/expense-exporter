package com.example.expenseexporter.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class InvoiceTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    private static Invoice invoice(String vendor, String amount) {
        return new Invoice("INV-0001", "E-001", LocalDate.of(2026, 10, 2), vendor, "", new BigDecimal(amount), EUR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "1", "45.5", "45.50", "1000000.00"})
    void acceptsPositiveAmountsWithAtMostTwoDecimals(String amount) {
        assertThat(invoice("Vendor", amount).amount()).isEqualTo(new BigDecimal(amount));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-1.00"})
    void rejectsAmountsThatAreNotPositive(String amount) {
        assertThatIllegalArgumentException().isThrownBy(() -> invoice("Vendor", amount))
                .withMessageContaining("greater than 0");
    }

    @Test
    void rejectsMoreThanTwoDecimals() {
        assertThatIllegalArgumentException().isThrownBy(() -> invoice("Vendor", "10.001"))
                .withMessageContaining("2 decimal places");
    }

    @Test
    void rejectsBlankVendorAndMissingFields() {
        assertThatIllegalArgumentException().isThrownBy(() -> invoice("  ", "10.00"));
        assertThatNullPointerException().isThrownBy(() -> new Invoice(
                "INV-0001", "E-001", null, "Vendor", "", BigDecimal.TEN, EUR));
    }
}
