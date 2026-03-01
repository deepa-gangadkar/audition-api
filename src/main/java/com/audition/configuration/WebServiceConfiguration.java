package com.audition.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;


@Configuration
public class WebServiceConfiguration implements WebMvcConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(WebServiceConfiguration.class);
    private static final String YEAR_MONTH_DAY_PATTERN = "yyyy-MM-dd";

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        //  1. allows for date format as yyyy-MM-dd
        mapper.setDateFormat(new SimpleDateFormat(YEAR_MONTH_DAY_PATTERN));
        //  2. Does not fail on unknown properties
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //  3. maps to camelCase
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        //  4. Does not include null values or empty values
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        //  5. does not write datas as timestamps.
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Register JavaTimeModule for java.time.* support
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    // -------------------------------
    // RestTemplate with logging + OpenTelemetry
    // Combines with Logging
    // Non-blocking and buffered RestTemplate configuration
    // -------------------------------
    @Bean
    public RestTemplate restTemplate(ObjectMapper objectMapper) {
        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(createClientFactory())
        );

        // Add a logging interceptor
        restTemplate.getInterceptors().add(new LoggingAndTracingInterceptor());

        return restTemplate;
    }

    private SimpleClientHttpRequestFactory createClientFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setOutputStreaming(false);
        return requestFactory;
    }

    // -------------------------------
    // Logging interceptor
    // -------------------------------
    private static class LoggingAndTracingInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(org.springframework.http.HttpRequest request,
                                            byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {

            // Automatically adds OpenTelemetry X-Trace-Id and X-Span-Id headers to all outgoing RestTemplate calls.
            Span currentSpan = Span.current();
            SpanContext context = currentSpan.getSpanContext();

            if (context.isValid()) {
                request.getHeaders().add("X-Trace-Id", context.getTraceId());
                request.getHeaders().add("X-Span-Id", context.getSpanId());
            }

            logRequest(request, body);
            ClientHttpResponse response = execution.execute(request, body);
            logResponse(response);

            return response;
        }

        private void logRequest(org.springframework.http.HttpRequest request, byte[] body) throws IOException {
            LOG.info("=== RestTemplate Request ===");
            LOG.info("URI         : {}", request.getURI());
            LOG.info("Method      : {}", request.getMethod());
            LOG.info("Headers     : {}", request.getHeaders());
            LOG.info("Request body: {}", new String(body, StandardCharsets.UTF_8));
        }

        private void logResponse(ClientHttpResponse response) throws IOException {
            String body = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            LOG.info("=== RestTemplate Response ===");
            LOG.info("Status code  : {}", response.getStatusCode());
            LOG.info("Status text  : {}", response.getStatusText());
            LOG.info("Headers      : {}", response.getHeaders());
            LOG.info("Response body: {}", body);
        }
    }
}
