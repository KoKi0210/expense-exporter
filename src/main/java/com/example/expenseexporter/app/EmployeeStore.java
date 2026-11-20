package com.example.expenseexporter.app;

import com.example.expenseexporter.domain.Employee;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class EmployeeStore {

    private final ConcurrentMap<String, Employee> employees = new ConcurrentSkipListMap<>();

    public void add(Employee employee) {
        Objects.requireNonNull(employee, "employee");
        if (employees.putIfAbsent(employee.id(), employee) != null) {
            throw new IllegalArgumentException("Employee " + employee.id() + " already exists");
        }
    }

    public Optional<Employee> findById(String id) {
        return Optional.ofNullable(employees.get(id));
    }

    public List<Employee> findAll() {
        return List.copyOf(employees.values());
    }
}
