package com.employee.employeeservice.controller;

import com.employee.employeeservice.application.exception.EmployeeNotFoundException;
import com.employee.employeeservice.application.service.GetEmployeeService;
import com.employee.employeeservice.application.service.ListEmployeesService;
import com.employee.employeeservice.dto.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeQueryController {

    private final GetEmployeeService getEmployeeService;
    private final ListEmployeesService listEmployeesService;

    @GetMapping
    public List<EmployeeResponse> list() {
        return listEmployeesService.execute().stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable("id") long id) {
        return EmployeeResponse.from(getEmployeeService.execute(id));
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> employeeNotFound(EmployeeNotFoundException exception) {
        return Map.of("error", "EMPLOYEE_NOT_FOUND", "message", exception.getMessage());
    }
}
