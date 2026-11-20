package com.example.expenseexporter.app;

import com.example.expenseexporter.domain.Invoice;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class InvoiceStore {

    private final ConcurrentMap<String, Invoice> invoices = new ConcurrentSkipListMap<>();

    public void add(Invoice invoice) {
        Objects.requireNonNull(invoice, "invoice");
        if (invoices.putIfAbsent(invoice.number(), invoice) != null) {
            throw new IllegalArgumentException("Invoice " + invoice.number() + " already exists");
        }
    }

    public Optional<Invoice> findByNumber(String number) {
        return Optional.ofNullable(invoices.get(number));
    }

    public List<Invoice> findAll() {
        return List.copyOf(invoices.values());
    }
}
