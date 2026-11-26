package com.employee.employeeservice.config;

import com.employee.employeeservice.application.port.out.EmployeeRepository;
import com.employee.employeeservice.application.service.CreateEmployeeService;
import com.employee.employeeservice.application.service.GetEmployeeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class EmployeeConfiguration {

    @Bean
    public GetEmployeeService getEmployeeService(EmployeeRepository employeeRepository) {
        return new GetEmployeeService(employeeRepository);
    }

    @Bean
    public CreateEmployeeService createEmployeeService(EmployeeRepository employeeRepository) {
        return new CreateEmployeeService(employeeRepository);
    }
}
