package com.example.expenseexporter.app;

import com.example.expenseexporter.domain.Employee;
import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.Position;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

public final class SampleData {

  public static final Currency EUR = Currency.getInstance("EUR");
  public static final Currency USD = Currency.getInstance("USD");

  private SampleData() {
  }

  public static void loadInto(EmployeeStore employeeStore, InvoiceStore invoiceStore) {
    employees().forEach(employeeStore::add);
    invoices().forEach(invoiceStore::add);
  }

  public static List<Employee> employees() {
    return List.of(
        new Employee("E-001", "Anna Müller", Position.INTERN, money("1500.00"), EUR),
        new Employee("E-002", "Георги Димитров", Position.JUNIOR, money("2400.00"), EUR),
        new Employee("E-003", "Maria O'Neil", Position.SENIOR, money("4200.00"), USD),
        new Employee("E-004", "Petar Ivanov", Position.MANAGER, money("6000.00"), EUR),
        new Employee("E-005", "Elena Koleva", Position.DIRECTOR, money("9000.00"), USD),
        new Employee("E-006", "Стефан Георгиев", Position.DIRECTOR, money("0.00"), EUR));
  }

  public static List<Invoice> invoices() {
    return List.of(
        new Invoice("INV-0001", "E-001", LocalDate.of(2026, 10, 2),
            "Office Depot", "Notebooks, pens", money("45.50"), EUR),
        new Invoice("INV-0002", "E-001", LocalDate.of(2026, 10, 9),
            "Amazon", "Monitor 27", money("210.00"), EUR),
        new Invoice("INV-0003", "E-002", LocalDate.of(2026, 10, 5),
            "Кафе Витоша", "Team coffee", money("38.20"), EUR),
        new Invoice("INV-0004", "E-003", LocalDate.of(2026, 10, 12),
            "Smith & Sons <Ltd>", "Conference ticket", money("650.00"), USD),
        new Invoice("INV-0005", "E-004", LocalDate.of(2026, 10, 14),
            "Hotel Rila", "2 nights, incl. breakfast", money("420.00"), EUR),
        new Invoice("INV-0006", "E-004", LocalDate.of(2026, 10, 20),
            "IT Supplies", "USB drive for C:\\Reports\\Q3", money("19.99"), EUR),
        new Invoice("INV-0007", "E-005", LocalDate.of(2026, 10, 22),
            "Grand Hotel", "Board dinner", money("1200.00"), USD),
        new Invoice("INV-0008", "E-006", LocalDate.of(2026, 10, 27),
            "Bulgaria Air", "Flight to Varna", money("750.00"), EUR));
  }

  private static BigDecimal money(String value) {
    return new BigDecimal(value);
  }
}
