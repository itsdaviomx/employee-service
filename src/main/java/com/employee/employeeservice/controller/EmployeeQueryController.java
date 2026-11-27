package com.employee.employeeservice.controller;

import com.employee.employeeservice.application.service.GetEmployeeService;
import com.employee.employeeservice.application.service.ListEmployeesService;
import com.employee.employeeservice.application.service.SearchEmployeesService;
import com.employee.employeeservice.dto.EmployeeResponse;
import com.employee.employeeservice.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Employees")
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeQueryController {

    private final GetEmployeeService getEmployeeService;
    private final ListEmployeesService listEmployeesService;
    private final SearchEmployeesService searchEmployeesService;

    @Operation(
            summary = "List all employees",
            description = "Returns active and inactive employees ordered by ID, or an empty array."
    )
    @ApiResponse(responseCode = "200", description = "Registered employees")
    @GetMapping
    public List<EmployeeResponse> list() {
        return listEmployeesService.execute().stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Operation(
            summary = "Search employees by partial name",
            description = "Case-insensitive match on first name, middle name or either last name, ordered by ID."
    )
    @ApiResponse(responseCode = "200", description = "Matching employees, or an empty array")
    @ApiResponse(responseCode = "400", description = "Name is missing, blank or longer than 100 characters",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/search")
    public List<EmployeeResponse> search(
            @Parameter(description = "Text contained in any name field", example = "ana")
            @RequestParam("name") String name
    ) {
        return searchEmployeesService.execute(name).stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Operation(summary = "Get an employee by ID")
    @ApiResponse(responseCode = "200", description = "Employee found")
    @ApiResponse(responseCode = "400", description = "ID is not a number",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable("id") long id) {
        return EmployeeResponse.from(getEmployeeService.execute(id));
    }
}
