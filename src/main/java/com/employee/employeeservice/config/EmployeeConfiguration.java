package com.employee.employeeservice.config;

import com.employee.employeeservice.application.port.out.EmployeeRepository;
import com.employee.employeeservice.application.service.CreateEmployeeService;
import com.employee.employeeservice.application.service.GetEmployeeService;
import com.employee.employeeservice.application.service.ListEmployeesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class EmployeeConfiguration {

    @Bean
    public ListEmployeesService listEmployeesService(EmployeeRepository employeeRepository) {
        return new ListEmployeesService(employeeRepository);
    }

    @Bean
    public GetEmployeeService getEmployeeService(EmployeeRepository employeeRepository) {
        return new GetEmployeeService(employeeRepository);
    }

    @Bean
    public CreateEmployeeService createEmployeeService(EmployeeRepository employeeRepository) {
        return new CreateEmployeeService(employeeRepository);
    }
}
