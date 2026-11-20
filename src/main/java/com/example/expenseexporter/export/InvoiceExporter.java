package com.example.expenseexporter.export;

import com.example.expenseexporter.domain.ValidatedExpense;

import java.util.List;
import java.util.Objects;

/**
 * Template Method
 *
 * <p>{@link #export(List)} is the template method. It is {@code final}, so every format follows the
 * same steps in the same order:
 */
public abstract class InvoiceExporter {

  public final String export(List<ValidatedExpense> expenses) {
    List<ValidatedExpense> rows = List.copyOf(Objects.requireNonNull(expenses, "expenses"));
    StringBuilder out = new StringBuilder();
    writeHeader(out);
    for (int i = 0; i < rows.size(); i++) {
      if (i > 0) {
        writeSeparator(out);
      }
      writeRecord(out, rows.get(i));
    }
    writeFooter(out, ExportSummary.of(rows));
    return out.toString();
  }

  public abstract ExportFormat format();

  protected void writeHeader(StringBuilder out) {
  }

  protected void writeSeparator(StringBuilder out) {
  }

  protected abstract void writeRecord(StringBuilder out, ValidatedExpense expense);

  protected void writeFooter(StringBuilder out, ExportSummary summary) {
  }
}
