package com.employee.employeeservice.controller;

import com.employee.employeeservice.application.service.GetEmployeeService;
import com.employee.employeeservice.application.service.ListEmployeesService;
import com.employee.employeeservice.application.service.SearchEmployeesService;
import com.employee.employeeservice.dto.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeQueryController {

    private final GetEmployeeService getEmployeeService;
    private final ListEmployeesService listEmployeesService;
    private final SearchEmployeesService searchEmployeesService;

    @GetMapping
    public List<EmployeeResponse> list() {
        return listEmployeesService.execute().stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @GetMapping("/search")
    public List<EmployeeResponse> search(@RequestParam("name") String name) {
        return searchEmployeesService.execute(name).stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable("id") long id) {
        return EmployeeResponse.from(getEmployeeService.execute(id));
    }
}
