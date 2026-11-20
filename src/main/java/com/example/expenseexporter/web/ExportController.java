package com.example.expenseexporter.web;

import com.example.expenseexporter.app.ExpenseReportService;
import com.example.expenseexporter.export.ExportFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Locale;

@RestController
@RequestMapping("/api/exports")
public class ExportController {

  private final ExpenseReportService service;

  public ExportController(ExpenseReportService service) {
    this.service = service;
  }

  @GetMapping("/{format}")
  public ResponseEntity<String> export(@PathVariable String format) {
    ExportFormat exportFormat = parseFormat(format);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(exportFormat.mediaType() + ";charset=UTF-8"))
        .body(service.export(exportFormat));
  }

  private static ExportFormat parseFormat(String format) {
    return Arrays.stream(ExportFormat.values())
        .filter(candidate -> candidate.name().equals(format.toUpperCase(Locale.ROOT)))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "Unknown export format '" + format + "', expected csv, json or xml"));
  }
}
