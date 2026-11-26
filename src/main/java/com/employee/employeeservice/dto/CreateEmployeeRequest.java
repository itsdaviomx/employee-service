package com.employee.employeeservice.dto;

import com.employee.employeeservice.application.port.in.CreateEmployeeCommand;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

public record CreateEmployeeRequest(

        @NotBlank
        String firstName,

        String middleName,

        @NotBlank
        String paternalLastName,

        String maternalLastName,

        @PositiveOrZero
        Integer age,

        @NotBlank
        String sex,

        @NotNull
        @PastOrPresent
        @JsonFormat(pattern = "dd-MM-yyyy")
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