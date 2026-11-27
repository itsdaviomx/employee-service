package com.employee.employeeservice.dto;

import com.employee.employeeservice.application.port.in.CreateEmployeeCommand;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.UUID;

public record CreateEmployeeRequest(

        @Schema(description = "Optional client-generated key; repeat it on retries to avoid duplicates")
        UUID requestId,

        @NotBlank
        String firstName,

        String middleName,

        @NotBlank
        String paternalLastName,

        String maternalLastName,

        @Schema(description = "Optional; when sent it must match the birth date")
        @PositiveOrZero
        Integer age,

        @NotBlank
        String sex,

        @NotNull
        @PastOrPresent
        @JsonFormat(pattern = ApiDateFormat.PATTERN)
        @Schema(type = "string", pattern = ApiDateFormat.REGEX, example = ApiDateFormat.EXAMPLE)
        LocalDate birthDate,

        @NotBlank
        String jobTitle,

        @NotNull
        Boolean active

) {

    public CreateEmployeeCommand toCommand() {
        return new CreateEmployeeCommand(
                firstName,
                middleName,
                paternalLastName,
                maternalLastName,
                sex,
                birthDate,
                age,
                jobTitle,
                active
        );
    }
}
