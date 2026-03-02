package com.audition.configuration;

import com.audition.infrastructure.http.LoggingAndTracingInterceptor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;


@Configuration
public class WebServiceConfiguration implements WebMvcConfigurer {
    private static final String YEAR_MONTH_DAY_PATTERN = "yyyy-MM-dd";

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        //  1. allows for date format as yyyy-MM-dd.
        mapper.setDateFormat(new SimpleDateFormat(YEAR_MONTH_DAY_PATTERN));
        //  2. Does not fail on unknown properties.
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //  3. maps to camelCase.
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        //  4. Does not include null values or empty values.
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        //  5. does not write datas as timestamps.
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Register JavaTimeModule for java.time. support.
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    // -------------------------------
    // RestTemplate with logging + OpenTelemetry.
    // Combines with Logging.
    // Non-blocking and buffered RestTemplate configuration.
    // -------------------------------
    @Bean
    public RestTemplate restTemplate(ObjectMapper objectMapper,
                                     LoggingAndTracingInterceptor loggingAndTracingInterceptor) {
        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(createClientFactory())
        );
        // Add a logging interceptor
        restTemplate.getInterceptors().add(loggingAndTracingInterceptor);
        return restTemplate;
    }

    @Bean
    public LoggingAndTracingInterceptor loggingAndTracingInterceptor() {
        return new LoggingAndTracingInterceptor();
    }

    private SimpleClientHttpRequestFactory createClientFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setOutputStreaming(false);
        return requestFactory;
    }
}
