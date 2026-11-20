package com.example.expenseexporter.export;

import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.ValidatedExpense;
import com.example.expenseexporter.domain.ValidationResult;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

public class XmlExporter extends InvoiceExporter {

  @Override
  public ExportFormat format() {
    return ExportFormat.XML;
  }

  @Override
  protected void writeHeader(StringBuilder out) {
    out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    out.append("<expenseReport>\n");
    out.append("  <invoices>\n");
  }

  @Override
  protected void writeRecord(StringBuilder out, ValidatedExpense expense) {
    Invoice invoice = expense.invoice();
    ValidationResult result = expense.result();
    out.append("    <invoice>\n");
    element(out, "invoiceNumber", invoice.number());
    element(out, "issueDate", invoice.issueDate().toString());
    element(out, "employeeId", expense.employee().id());
    element(out, "employeeName", expense.employee().fullName());
    element(out, "position", expense.employee().position().name());
    element(out, "vendor", invoice.vendor());
    element(out, "description", invoice.description());
    out.append("      <amount currency=\"")
        .append(escape(invoice.currency().getCurrencyCode()))
        .append("\">")
        .append(invoice.amount().toPlainString())
        .append("</amount>\n");
    if (result.limit() != null) {
      element(out, "limit", result.limit().toPlainString());
    }
    element(out, "status", result.status().name());
    element(out, "rule", result.ruleDescription());
    if (result.message() != null) {
      element(out, "message", result.message());
    }
    out.append("    </invoice>\n");
  }

  @Override
  protected void writeFooter(StringBuilder out, ExportSummary summary) {
    out.append("  </invoices>\n");
    out.append("  <summary>\n");
    out.append("    <count>").append(summary.count()).append("</count>\n");
    out.append("    <approved>").append(summary.approved()).append("</approved>\n");
    out.append("    <rejected>").append(summary.rejected()).append("</rejected>\n");
    out.append("    <errors>").append(summary.errors()).append("</errors>\n");
    if (summary.totalsByCurrency().isEmpty()) {
      out.append("    <totals/>\n");
    } else {
      out.append("    <totals>\n");
      for (Map.Entry<Currency, BigDecimal> total : summary.totalsByCurrency().entrySet()) {
        out.append("      <total currency=\"")
            .append(escape(total.getKey().getCurrencyCode()))
            .append("\">")
            .append(total.getValue().toPlainString())
            .append("</total>\n");
      }
      out.append("    </totals>\n");
    }
    out.append("  </summary>\n");
    out.append("</expenseReport>\n");
  }

  private static void element(StringBuilder out, String name, String text) {
    out.append("      <").append(name).append('>')
        .append(escape(text))
        .append("</").append(name).append(">\n");
  }


  private static String escape(String value) {
    StringBuilder out = new StringBuilder(value.length());
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      switch (c) {
        case '&' -> out.append("&amp;");
        case '<' -> out.append("&lt;");
        case '>' -> out.append("&gt;");
        case '"' -> out.append("&quot;");
        case '\'' -> out.append("&apos;");
        case '\r' -> out.append("&#13;");
        case '\t', '\n' -> out.append(c);
        default -> {
          if (c < 0x20 || c == '￾' || c == '￿') {
            throw new IllegalArgumentException(String.format(
                "Character U+%04X at index %d is not allowed in XML 1.0", (int) c, i));
          }
          out.append(c);
        }
      }
    }
    return out.toString();
  }
}
