package com.example.expenseexporter.export;

import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.ValidatedExpense;
import com.example.expenseexporter.domain.ValidationResult;

import java.math.BigDecimal;
import java.util.List;

public class CsvExporter extends InvoiceExporter {

  private static final String LINE_END = "\r\n";
  private static final List<String> COLUMNS = List.of(
      "invoiceNumber", "issueDate", "employeeId", "employeeName", "position", "vendor",
      "description", "amount", "currency", "limit", "status", "rule", "message");

  @Override
  public ExportFormat format() {
    return ExportFormat.CSV;
  }

  @Override
  protected void writeHeader(StringBuilder out) {
    out.append(String.join(",", COLUMNS)).append(LINE_END);
  }

  @Override
  protected void writeRecord(StringBuilder out, ValidatedExpense expense) {
    Invoice invoice = expense.invoice();
    ValidationResult result = expense.result();
    List<String> fields = List.of(
        invoice.number(),
        invoice.issueDate().toString(),
        expense.employee().id(),
        expense.employee().fullName(),
        expense.employee().position().name(),
        invoice.vendor(),
        invoice.description(),
        invoice.amount().toPlainString(),
        invoice.currency().getCurrencyCode(),
        plain(result.limit()),
        result.status().name(),
        result.ruleDescription(),
        nullToEmpty(result.message()));
    out.append(String.join(",", fields.stream().map(CsvExporter::escape).toList())).append(LINE_END);
  }

  private static String plain(BigDecimal value) {
    return value == null ? "" : value.toPlainString();
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private static String escape(String value) {
    if (value == null) {
      return "";
    }
    boolean needsQuotes = value.indexOf(',') >= 0
        || value.indexOf('"') >= 0
        || value.indexOf('\n') >= 0
        || value.indexOf('\r') >= 0;
    if (!needsQuotes) {
      return value;
    }
    return '"' + value.replace("\"", "\"\"") + '"';
  }
}
