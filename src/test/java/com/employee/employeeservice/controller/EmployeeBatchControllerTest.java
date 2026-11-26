package com.employee.employeeservice.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeBatchControllerTest extends EmployeeBatchHttpSupport {

    @Test
    void shouldKeepBothValidEmployeesWhenTheMiddleItemFails() throws Exception {
        var result = submit(200, employee("Ana"), employee("Luis").put("age", 0), employee("Eva"));

        assertThat(result.findValuesAsText("status")).containsExactly("CREATED", "INVALID", "CREATED");
        assertThat(result.get(1).get("position").asInt()).isEqualTo(2);
        assertThat(employeeCount()).isEqualTo(2);
    }
}
