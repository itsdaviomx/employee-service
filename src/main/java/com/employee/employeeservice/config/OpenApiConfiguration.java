package com.employee.employeeservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    private static final String BASIC_AUTH = "basicAuth";

    @Bean
    public OpenAPI employeeServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Employee Service API")
                        .version("v1")
                        .description("Create, read, update, delete and search employees. Dates use dd-MM-yyyy. "
                                + "HTTP Basic: READER can read; ADMIN can also create, update and delete."))
                .components(new Components().addSecuritySchemes(BASIC_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH));
    }
}
