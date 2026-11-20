package com.example.expenseexporter.web;

import com.example.expenseexporter.app.EmployeeStore;
import com.example.expenseexporter.domain.Employee;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

  private final EmployeeStore employees;

  public EmployeeController(EmployeeStore employees) {
    this.employees = employees;
  }

  @GetMapping
  public List<Employee> all() {
    return employees.findAll();
  }

  @GetMapping("/{id}")
  public Employee one(@PathVariable String id) {
    return employees.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee " + id + " not found"));
  }

  @PostMapping
  public ResponseEntity<Employee> add(@RequestBody Employee employee) {
    employees.add(employee);
    return ResponseEntity
        .created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(employee.id()).toUri())
        .body(employee);
  }
}
