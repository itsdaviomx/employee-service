package com.employee.employeeservice.infrastructure.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(OutputCaptureExtension.class)
class RequestLoggingFilterTest {

    private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    private static final FilterChain OK = (request, response) -> { };

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void shouldGenerateACorrelationIdAndShareItWithTheRequest() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seenDuringRequest = new AtomicReference<>();

        filter.doFilter(new MockHttpServletRequest("GET", "/employees"), response,
                (req, res) -> seenDuringRequest.set(MDC.get(RequestLoggingFilter.CORRELATION_ID_KEY)));

        String correlationId = response.getHeader(RequestLoggingFilter.CORRELATION_ID_HEADER);
        assertThat(correlationId).matches(UUID_REGEX);
        assertThat(seenDuringRequest.get()).isEqualTo(correlationId);
        assertThat(MDC.get(RequestLoggingFilter.CORRELATION_ID_KEY)).isNull();
    }

    @Test
    void shouldReuseAValidCorrelationIdSentByTheClient() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/employees");
        request.addHeader(RequestLoggingFilter.CORRELATION_ID_HEADER, "order-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, OK);

        assertThat(response.getHeader(RequestLoggingFilter.CORRELATION_ID_HEADER)).isEqualTo("order-123");
    }

    @Test
    void shouldReplaceAnInvalidCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/employees");
        request.addHeader(RequestLoggingFilter.CORRELATION_ID_HEADER, "bad id\r\nINJECTED");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, OK);

        assertThat(response.getHeader(RequestLoggingFilter.CORRELATION_ID_HEADER)).matches(UUID_REGEX);
    }

    @Test
    void shouldLogTheRequestAndMaskSensitiveHeaders(CapturedOutput output) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/employees");
        request.addHeader("Accept", "application/json");
        request.addHeader("Authorization", "Bearer secret-token");
        request.addHeader("Cookie", "SESSION=abc123");

        filter.doFilter(request, new MockHttpServletResponse(),
                (req, res) -> ((HttpServletResponse) res).setStatus(201));

        assertThat(output)
                .contains("HTTP POST /employees -> 201 in ")
                .contains("Accept=application/json")
                .contains("Authorization=***")
                .contains("Cookie=***")
                .doesNotContain("secret-token")
                .doesNotContain("SESSION=abc123");
    }

    @Test
    void shouldNeutralizeLineBreaksInHeaders(CapturedOutput output) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/employees");
        request.addHeader("User-Agent", "curl\r\nFAKE ENTRY");

        filter.doFilter(request, new MockHttpServletResponse(), OK);

        assertThat(output)
                .contains("User-Agent=curl__FAKE ENTRY")
                .doesNotContain("\nFAKE ENTRY");
    }

    @Test
    void shouldLeaveTheQueryStringOutOfTheLog(CapturedOutput output) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/employees/search");
        request.setQueryString("name=ana");
        request.addParameter("name", "ana");

        filter.doFilter(request, new MockHttpServletResponse(), OK);

        assertThat(output)
                .contains("HTTP GET /employees/search -> 200")
                .doesNotContain("name=ana");
    }

    @Test
    void shouldLogAServerErrorWhenTheRequestFails(CapturedOutput output) {
        FilterChain failing = (req, res) -> {
            throw new IllegalStateException("boom");
        };

        assertThatThrownBy(() -> filter.doFilter(
                new MockHttpServletRequest("GET", "/employees"), new MockHttpServletResponse(), failing))
                .isInstanceOf(IllegalStateException.class);

        assertThat(output).contains("HTTP GET /employees -> 500 in ");
        assertThat(MDC.get(RequestLoggingFilter.CORRELATION_ID_KEY)).isNull();
    }

    @Test
    void shouldSkipHealthProbes(CapturedOutput output) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest("GET", "/actuator/health/readiness"), response, OK);

        assertThat(output).doesNotContain("HTTP GET /actuator/health");
        assertThat(response.getHeader(RequestLoggingFilter.CORRELATION_ID_HEADER)).isNull();
    }
}
