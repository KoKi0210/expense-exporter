package com.example.expenseexporter.export;

import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.ValidatedExpense;
import com.example.expenseexporter.domain.ValidationResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class JsonExporter extends InvoiceExporter {

  private static final String RECORD_INDENT = "    ";
  private static final String FIELD_INDENT = "      ";
  private static final String SUMMARY_INDENT = "    ";

  @Override
  public ExportFormat format() {
    return ExportFormat.JSON;
  }

  @Override
  protected void writeHeader(StringBuilder out) {
    out.append("{\n  \"invoices\": [");
  }

  @Override
  protected void writeSeparator(StringBuilder out) {
    out.append(',');
  }

  @Override
  protected void writeRecord(StringBuilder out, ValidatedExpense expense) {
    Invoice invoice = expense.invoice();
    ValidationResult result = expense.result();
    out.append('\n').append(RECORD_INDENT).append("{\n");
    appendFields(out, FIELD_INDENT, List.of(
        field("invoiceNumber", quote(invoice.number())),
        field("issueDate", quote(invoice.issueDate().toString())),
        field("employeeId", quote(expense.employee().id())),
        field("employeeName", quote(expense.employee().fullName())),
        field("position", quote(expense.employee().position().name())),
        field("vendor", quote(invoice.vendor())),
        field("description", quote(invoice.description())),
        field("amount", number(invoice.amount())),
        field("currency", quote(invoice.currency().getCurrencyCode())),
        field("limit", number(result.limit())),
        field("status", quote(result.status().name())),
        field("rule", quote(result.ruleDescription())),
        field("message", quote(result.message()))));
    out.append('\n').append(RECORD_INDENT).append('}');
  }

  @Override
  protected void writeFooter(StringBuilder out, ExportSummary summary) {
    out.append(summary.count() == 0 ? "]" : "\n  ]").append(",\n");
    out.append("  \"summary\": {\n");

    List<String> totals = summary.totalsByCurrency().entrySet().stream()
        .map(total -> field(total.getKey().getCurrencyCode(), number(total.getValue())))
        .toList();
    String totalsObject = totals.isEmpty()
        ? "{}"
        : "{\n" + FIELD_INDENT + String.join(",\n" + FIELD_INDENT, totals) + "\n" + SUMMARY_INDENT + "}";

    List<String> fields = new ArrayList<>(List.of(
        field("count", Integer.toString(summary.count())),
        field("approved", Integer.toString(summary.approved())),
        field("rejected", Integer.toString(summary.rejected())),
        field("errors", Integer.toString(summary.errors()))));
    fields.add(field("totalsByCurrency", totalsObject));
    appendFields(out, SUMMARY_INDENT, fields);
    out.append("\n  }\n}\n");
  }

  private static void appendFields(StringBuilder out, String indent, List<String> fields) {
    out.append(indent).append(String.join(",\n" + indent, fields));
  }

  private static String field(String name, String jsonValue) {
    return quote(name) + ": " + jsonValue;
  }

  private static String number(BigDecimal value) {
    return value == null ? "null" : value.toPlainString();
  }

  private static String quote(String value) {
    if (value == null) {
      return "null";
    }
    StringBuilder out = new StringBuilder(value.length() + 2);
    out.append('"');
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '"' -> out.append("\\\"");
        case '\\' -> out.append("\\\\");
        case '\b' -> out.append("\\b");
        case '\f' -> out.append("\\f");
        case '\n' -> out.append("\\n");
        case '\r' -> out.append("\\r");
        case '\t' -> out.append("\\t");
        default -> {
          if (c < 0x20) {
            out.append(String.format("\\u%04x", (int) c));
          } else {
            out.append(c);
          }
        }
      }
    }
    return out.append('"').toString();
  }
}
