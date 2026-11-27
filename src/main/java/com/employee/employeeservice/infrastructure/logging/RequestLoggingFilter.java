package com.employee.employeeservice.infrastructure.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Logs one line per HTTP request with its method, path, status, duration and headers,
 * and tags every log line written during the request with a correlation ID.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_KEY = "correlationId";

    private static final Pattern VALID_CORRELATION_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private static final Set<String> MASKED_HEADERS =
            Set.of("authorization", "proxy-authorization", "cookie", "set-cookie", "x-api-key");
    private static final String MASK = "***";
    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("\\p{Cntrl}");
    private static final int MAX_VALUE_LENGTH = 200;
    private static final String HEALTH_PATH = "/actuator/health";
    private static final int SERVER_ERROR = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

    // Health probes run every few seconds; logging them would bury real traffic.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith(HEALTH_PATH);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        String correlationId = Optional.ofNullable(request.getHeader(CORRELATION_ID_HEADER))
                .filter(id -> VALID_CORRELATION_ID.matcher(id).matches())
                .orElseGet(() -> UUID.randomUUID().toString());

        MDC.put(CORRELATION_ID_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        long start = System.nanoTime();
        boolean failed = false;
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException exception) {
            failed = true;
            throw exception;
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            int status = failed ? SERVER_ERROR : response.getStatus();
            // The query string is left out: it can carry personal data or tokens.
            log.info("HTTP {} {} -> {} in {} ms headers={}",
                    request.getMethod(), sanitize(request.getRequestURI()), status, elapsedMs, headers(request));
            MDC.remove(CORRELATION_ID_KEY);
        }
    }

    private static Map<String, String> headers(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(
                        RequestLoggingFilter::sanitize,
                        name -> headerValue(request, name),
                        (first, second) -> first,
                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)
                ));
    }

    private static String headerValue(HttpServletRequest request, String name) {
        if (MASKED_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
            return MASK;
        }
        return sanitize(String.join(",", Collections.list(request.getHeaders(name))));
    }

    // Removes line breaks and other control characters so a client cannot forge log lines.
    private static String sanitize(String value) {
        String clean = CONTROL_CHARACTERS.matcher(value).replaceAll("_");
        return clean.length() > MAX_VALUE_LENGTH ? clean.substring(0, MAX_VALUE_LENGTH) + "..." : clean;
    }
}
