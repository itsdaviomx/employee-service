package com.employee.employeeservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    @Bean
    public OpenAPI employeeServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Employee Service API")
                .version("v1")
                .description("Create, read, update, delete and search employees. Dates use dd-MM-yyyy."));
    }
}
