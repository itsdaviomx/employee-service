package com.employee.employeeservice.config;

import com.employee.employeeservice.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfiguration {

    public static final String ADMIN = "ADMIN";
    public static final String READER = "READER";

    private static final String REALM_HEADER = "Basic realm=\"employee-service\"";
    private static final String[] PUBLIC_PATHS = {
            "/actuator/health", "/actuator/health/**",
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        AuthenticationEntryPoint unauthorized = (request, response, exception) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, REALM_HEADER);
            writeError(response, objectMapper, HttpServletResponse.SC_UNAUTHORIZED,
                    new ErrorResponse("UNAUTHORIZED", "Valid credentials are required"));
        };
        AccessDeniedHandler forbidden = (request, response, exception) ->
                writeError(response, objectMapper, HttpServletResponse.SC_FORBIDDEN,
                        new ErrorResponse("FORBIDDEN", "Your role cannot perform this operation"));

        http
                // Stateless API: every request carries its credentials and no session cookie exists,
                // so there is no cookie for a CSRF attack to ride on.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .antMatchers(PUBLIC_PATHS).permitAll()
                        .mvcMatchers(HttpMethod.GET, "/employees/**").hasAnyRole(READER, ADMIN)
                        .mvcMatchers("/employees/**").hasRole(ADMIN)
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.authenticationEntryPoint(unauthorized))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler(forbidden));
        return http.build();
    }

    private static void writeError(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            int status,
            ErrorResponse body
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
