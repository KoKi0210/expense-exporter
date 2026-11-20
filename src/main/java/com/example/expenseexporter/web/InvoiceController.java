package com.example.expenseexporter.web;

import com.example.expenseexporter.app.EmployeeStore;
import com.example.expenseexporter.app.ExpenseReportService;
import com.example.expenseexporter.app.InvoiceStore;
import com.example.expenseexporter.domain.Employee;
import com.example.expenseexporter.domain.Invoice;
import com.example.expenseexporter.domain.ValidatedExpense;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

  private final InvoiceStore invoices;
  private final EmployeeStore employees;
  private final ExpenseReportService service;

  public InvoiceController(InvoiceStore invoices, EmployeeStore employees, ExpenseReportService service) {
    this.invoices = invoices;
    this.employees = employees;
    this.service = service;
  }

  @GetMapping
  public List<Invoice> all() {
    return invoices.findAll();
  }

  @GetMapping("/{number}")
  public Invoice one(@PathVariable String number) {
    return invoices.findByNumber(number)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice " + number + " not found"));
  }

  @GetMapping("/validation")
  public List<ValidatedExpense> validation() {
    return service.validateAll();
  }

  @PostMapping
  public ResponseEntity<Invoice> add(@RequestBody Invoice invoice) {
    Employee employee = employees.findById(invoice.employeeId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Employee " + invoice.employeeId() + " not found"));
    if (!invoice.currency().equals(employee.salaryCurrency())) {
      throw new IllegalArgumentException("Invoice currency " + invoice.currency().getCurrencyCode()
          + " differs from the salary currency " + employee.salaryCurrency().getCurrencyCode()
          + " of employee " + employee.id());
    }
    invoices.add(invoice);
    return ResponseEntity
        .created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{number}").buildAndExpand(invoice.number()).toUri())
        .body(invoice);
  }
}
