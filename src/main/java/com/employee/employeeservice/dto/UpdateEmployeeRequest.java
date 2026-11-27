package com.employee.employeeservice.dto;

import com.employee.employeeservice.application.port.in.UpdateEmployeeCommand;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.CodePointLength;

import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

import static com.employee.employeeservice.dto.EmployeeFieldLimits.MAX_TEXT_LENGTH;

public record UpdateEmployeeRequest(

        @Pattern(regexp = UpdateEmployeeRequest.NOT_BLANK, message = UpdateEmployeeRequest.BLANK_MESSAGE)
        @CodePointLength(max = MAX_TEXT_LENGTH)
        @Schema(maxLength = MAX_TEXT_LENGTH)
        String firstName,

        @CodePointLength(max = MAX_TEXT_LENGTH)
        @Schema(maxLength = MAX_TEXT_LENGTH)
        String middleName,

        @Pattern(regexp = UpdateEmployeeRequest.NOT_BLANK, message = UpdateEmployeeRequest.BLANK_MESSAGE)
        @CodePointLength(max = MAX_TEXT_LENGTH)
        @Schema(maxLength = MAX_TEXT_LENGTH)
        String paternalLastName,

        @CodePointLength(max = MAX_TEXT_LENGTH)
        @Schema(maxLength = MAX_TEXT_LENGTH)
        String maternalLastName,

        @Schema(description = "Optional; when sent it must match the resulting birth date")
        @PositiveOrZero
        Integer age,

        @Pattern(regexp = UpdateEmployeeRequest.NOT_BLANK, message = UpdateEmployeeRequest.BLANK_MESSAGE)
        @CodePointLength(max = MAX_TEXT_LENGTH)
        @Schema(maxLength = MAX_TEXT_LENGTH)
        String sex,

        @PastOrPresent
        @JsonDeserialize(using = BirthDateDeserializer.class)
        @Schema(type = "string", pattern = ApiDateFormat.REGEX, example = ApiDateFormat.EXAMPLE)
        LocalDate birthDate,

        @Pattern(regexp = UpdateEmployeeRequest.NOT_BLANK, message = UpdateEmployeeRequest.BLANK_MESSAGE)
        @CodePointLength(max = MAX_TEXT_LENGTH)
        @Schema(maxLength = MAX_TEXT_LENGTH)
        String jobTitle,

        Boolean active

) {

    // @NotBlank would reject null, which here means "keep the current value".
    static final String NOT_BLANK = "(?s).*\\S.*";
    static final String BLANK_MESSAGE = "must not be blank";

    public UpdateEmployeeCommand toCommand() {
        return new UpdateEmployeeCommand(
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
