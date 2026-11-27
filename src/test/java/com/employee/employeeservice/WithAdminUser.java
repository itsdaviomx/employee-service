package com.employee.employeeservice;

import com.employee.employeeservice.config.SecurityConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runs a test as an authenticated ADMIN with the real security rules. Web slice tests
 * (@WebMvcTest) do not scan @Configuration classes, so the rules are imported here.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(SecurityConfiguration.class)
@WithMockUser(username = "admin", roles = SecurityConfiguration.ADMIN)
public @interface WithAdminUser {
}
