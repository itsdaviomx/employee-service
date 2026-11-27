package com.employee.employeeservice.controller;

import com.employee.employeeservice.dto.CreateEmployeesRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;

@ControllerAdvice
public class BatchBodyAdvice extends RequestBodyAdviceAdapter {

    static final int MAX_BYTES = 1024 * 1024;

    static class PayloadTooLarge extends RuntimeException {}

    @Override
    public boolean supports(MethodParameter parameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return targetType == CreateEmployeesRequest.class;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage input, MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        if (input.getHeaders().getContentLength() > MAX_BYTES) {
            throw new PayloadTooLarge();
        }
        // Also enforce the limit when the client streams the body without a Content-Length.
        byte[] bytes = input.getBody().readNBytes(MAX_BYTES + 1);
        if (bytes.length > MAX_BYTES) {
            throw new PayloadTooLarge();
        }
        return new MappingJacksonInputMessage(new ByteArrayInputStream(bytes), input.getHeaders());
    }
}
