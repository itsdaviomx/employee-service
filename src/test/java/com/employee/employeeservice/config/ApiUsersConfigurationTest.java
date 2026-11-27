package com.employee.employeeservice.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiUsersConfigurationTest {

    private final ApiUsersConfiguration configuration = new ApiUsersConfiguration();
    private final PasswordEncoder encoder = configuration.passwordEncoder();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void shouldRefuseToStartWithABlankPassword(String blank) {
        assertThatThrownBy(() -> configuration.apiUsers("admin", blank, "reader", "reader-secret", encoder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("API_ADMIN_PASSWORD must be set to a non-blank password");

        assertThatThrownBy(() -> configuration.apiUsers("admin", "admin-secret", "reader", blank, encoder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("API_READER_PASSWORD must be set to a non-blank password");
    }

    @Test
    void shouldStorePasswordsAsBcryptHashesWithTheirRoles() {
        UserDetailsService users = configuration.apiUsers("admin", "admin-secret", "reader", "reader-secret", encoder);

        var admin = users.loadUserByUsername("admin");
        assertThat(admin.getPassword()).startsWith("{bcrypt}").doesNotContain("admin-secret");
        assertThat(encoder.matches("admin-secret", admin.getPassword())).isTrue();
        assertThat(admin.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_ADMIN");

        assertThat(users.loadUserByUsername("reader").getAuthorities())
                .extracting(Object::toString).containsExactly("ROLE_READER");
    }
}
