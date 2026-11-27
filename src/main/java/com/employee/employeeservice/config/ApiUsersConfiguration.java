package com.employee.employeeservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * API users. Passwords come only from the environment and are kept as BCrypt hashes in memory.
 * Kept apart from {@link SecurityConfiguration} so web slice tests can load the rules without users.
 */
@Configuration(proxyBeanMethods = false)
public class ApiUsersConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService apiUsers(
            @Value("${app.security.admin.username}") String adminUsername,
            @Value("${app.security.admin.password}") String adminPassword,
            @Value("${app.security.reader.username}") String readerUsername,
            @Value("${app.security.reader.password}") String readerPassword,
            PasswordEncoder passwordEncoder
    ) {
        return new InMemoryUserDetailsManager(
                User.withUsername(adminUsername)
                        .password(passwordEncoder.encode(requirePassword(adminPassword, "API_ADMIN_PASSWORD")))
                        .roles(SecurityConfiguration.ADMIN)
                        .build(),
                User.withUsername(readerUsername)
                        .password(passwordEncoder.encode(requirePassword(readerPassword, "API_READER_PASSWORD")))
                        .roles(SecurityConfiguration.READER)
                        .build()
        );
    }

    // A missing variable already stops the startup; an empty one would let anyone in with an empty password.
    static String requirePassword(String password, String variable) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(variable + " must be set to a non-blank password");
        }
        return password;
    }
}
