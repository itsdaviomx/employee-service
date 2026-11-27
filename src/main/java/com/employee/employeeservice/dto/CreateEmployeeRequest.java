package com.employee.employeeservice.dto;

import com.employee.employeeservice.application.port.in.CreateEmployeeCommand;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.CodePointLength;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.UUID;

import static com.employee.employeeservice.domain.model.Employee.MAX_TEXT_LENGTH;

public record CreateEmployeeRequest(

        @Schema(description = "Optional client-generated key; repeat it on retries to avoid duplicates")
        UUID requestId,

        @NotBlank
        @CodePointLength(max = MAX_TEXT_LENGTH)
        @Schema(maxLength = MAX_TEXT_LENGTH)
        String firstName,

        @CodePointLength(max = MAX_TEXT_LENGTH)
        @Schema(maxLength = MAX_TEXT_LENGTH)
        String middleName,

        @NotBlank
        @CodePointLength(max = MAX_TEXT_LENGTH)
        @Schema(maxLength = MAX_TEXT_LENGTH)
        String paternalLastName,

        @CodePointLength(max = MAX_TEXT_LENGTH)
        @Schema(maxLength = MAX_TEXT_LENGTH)
        String maternalLastName,

        @Schema(description = "Optional; when sent it must match the birth date")
        @PositiveOrZero
        Integer age,

        @NotBlank
        @CodePointLength(max = MAX_TEXT_LENGTH)
        @Schema(maxLength = MAX_TEXT_LENGTH)
        String sex,

        @NotNull
        @PastOrPresent
        @JsonDeserialize(using = BirthDateDeserializer.class)
        @Schema(type = "string", pattern = ApiDateFormat.REGEX, example = ApiDateFormat.EXAMPLE)
        LocalDate birthDate,

        @NotBlank
        @CodePointLength(max = MAX_TEXT_LENGTH)
        @Schema(maxLength = MAX_TEXT_LENGTH)
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
