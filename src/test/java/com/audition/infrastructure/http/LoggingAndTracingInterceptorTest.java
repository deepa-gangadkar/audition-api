package com.audition.infrastructure.http;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class LoggingAndTracingInterceptorTest {
    private LoggingAndTracingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new LoggingAndTracingInterceptor();
    }

    @Test
    void shouldAddTracingHeadersWhenSpanIsValid() throws IOException {

        // Arrange
        HttpRequest request = mock(HttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);
        when(request.getURI()).thenReturn(URI.create("http://localhost/test"));
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        byte[] body = "test-body".getBytes(StandardCharsets.UTF_8);

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = new MockClientHttpResponse("response".getBytes(), 200);

        when(execution.execute(request, body)).thenReturn(response);

        // Mock OpenTelemetry static call
        Span mockSpan = mock(Span.class);
        SpanContext mockContext = mock(SpanContext.class);

        when(mockContext.isValid()).thenReturn(true);
        when(mockContext.getTraceId()).thenReturn("trace-123");
        when(mockContext.getSpanId()).thenReturn("span-456");
        when(mockSpan.getSpanContext()).thenReturn(mockContext);

        try (MockedStatic<Span> spanMock = mockStatic(Span.class)) {
            spanMock.when(Span::current).thenReturn(mockSpan);

            // Act
            ClientHttpResponse result = interceptor.intercept(request, body, execution);

            // Assert
            assertEquals("trace-123", headers.getFirst("X-Trace-Id"));
            assertEquals("span-456", headers.getFirst("X-Span-Id"));
            assertNotNull(result);

            verify(execution).execute(request, body);
        }
    }

    @Test
    void shouldNotAddTracingHeadersWhenSpanIsInvalid() throws IOException {

        // Arrange
        HttpRequest request = mock(HttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);
        when(request.getURI()).thenReturn(URI.create("http://localhost/test"));
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        byte[] body = new byte[0];

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = new MockClientHttpResponse("response".getBytes(), 200);

        when(execution.execute(request, body)).thenReturn(response);

        Span mockSpan = mock(Span.class);
        SpanContext mockContext = mock(SpanContext.class);

        when(mockContext.isValid()).thenReturn(false);
        when(mockSpan.getSpanContext()).thenReturn(mockContext);

        try (MockedStatic<Span> spanMock = mockStatic(Span.class)) {
            spanMock.when(Span::current).thenReturn(mockSpan);

            // Act
            interceptor.intercept(request, body, execution);

            // Assert
            assertNull(headers.getFirst("X-Trace-Id"));
            assertNull(headers.getFirst("X-Span-Id"));
        }
    }
}
