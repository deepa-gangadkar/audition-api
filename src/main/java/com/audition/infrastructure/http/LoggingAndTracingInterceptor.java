package com.audition.infrastructure.http;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
public class LoggingAndTracingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(org.springframework.http.HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        addTracingHeaders(request);

        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);

        return response;
    }

    // Automatically adds OpenTelemetry X-Trace-Id and X-Span-Id headers to all outgoing RestTemplate calls.
    private void addTracingHeaders(org.springframework.http.HttpRequest request) {
        Span currentSpan = Span.current();
        SpanContext context = currentSpan.getSpanContext();

        if (context.isValid()) {
            request.getHeaders().add("X-Trace-Id", context.getTraceId());
            request.getHeaders().add("X-Span-Id", context.getSpanId());
        }
    }

    private void logRequest(org.springframework.http.HttpRequest request, byte[] body) throws IOException {
        log.info("=== RestTemplate Request ===");
        log.info("URI         : {}", request.getURI());
        log.info("Method      : {}", request.getMethod());
        log.info("Headers     : {}", request.getHeaders());
        log.info("Request body: {}", new String(body, StandardCharsets.UTF_8));
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        String body = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        log.info("=== RestTemplate Response ===");
        log.info("Status code  : {}", response.getStatusCode());
        log.info("Status text  : {}", response.getStatusText());
        log.info("Headers      : {}", response.getHeaders());
        log.info("Response body: {}", body);
    }
}
