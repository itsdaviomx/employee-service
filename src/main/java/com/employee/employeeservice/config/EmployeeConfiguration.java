package com.employee.employeeservice.config;

import com.employee.employeeservice.application.port.out.EmployeeRepository;
import com.employee.employeeservice.application.service.CreateEmployeeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class EmployeeConfiguration {

    @Bean
    public CreateEmployeeService createEmployeeService(EmployeeRepository employeeRepository) {
        return new CreateEmployeeService(employeeRepository);
    }
}