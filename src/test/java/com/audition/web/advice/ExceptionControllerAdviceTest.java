package com.audition.web.advice;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ExceptionControllerAdviceTest {
    private ExceptionControllerAdvice advice;

    private AuditionLogger auditionLogger;

    @Captor
    private ArgumentCaptor<Exception> exceptionCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        auditionLogger = mock(AuditionLogger.class);
        advice = new ExceptionControllerAdvice(auditionLogger);
    }

    // ---------------------------------------
    // HttpClientErrorException tests
    // ---------------------------------------
    @Test
    @DisplayName("handleHttpClientErrorException should return ProblemDetail with status from exception")
    void testHandleHttpClientException() {
        HttpClientErrorException exception =
                HttpClientErrorException.create(HttpStatusCode.valueOf(404), "Not Found", null, null, null);

        ProblemDetail result = advice.handleHttpClientException(exception);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(404);
        assertThat(result.getTitle()).isEqualTo(ExceptionControllerAdvice.DEFAULT_TITLE);
        assertThat(result.getDetail()).isEqualTo(exception.getMessage());

        verify(auditionLogger).logErrorWithException(any(), eq("HttpClientErrorException caught"), eq(exception));
        verify(auditionLogger).logStandardProblemDetail(any(), any(ProblemDetail.class), eq(exception));
    }

    // ---------------------------------------
    // Generic Exception tests
    // ---------------------------------------
    @Test
    @DisplayName("handleGenericException should return INTERNAL_SERVER_ERROR for unknown exception")
    void testHandleGenericExceptionFallback() {
        Exception exception = new Exception("Something went wrong");

        ProblemDetail result = advice.handleMainException(exception);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(500);
        assertThat(result.getTitle()).isEqualTo("API Error Occurred");
        assertThat(result.getDetail()).isEqualTo("Something went wrong");

        verify(auditionLogger).logErrorWithException(any(), eq("Unhandled Exception caught"), eq(exception));
        verify(auditionLogger).logStandardProblemDetail(any(), any(ProblemDetail.class), eq(exception));
    }

    @Test
    @DisplayName("handleMainException with null message returns default message")
    void testHandleGenericExceptionWithNullMessage() {
        Exception exception = new Exception((String) null);

        ProblemDetail result = advice.handleMainException(exception);

        assertThat(result.getDetail()).isEqualTo(ExceptionControllerAdvice.DEFAULT_MESSAGE);
    }

    @Test
    @DisplayName("handleMainException handles HttpRequestMethodNotSupportedException as METHOD_NOT_ALLOWED")
    void testHandleMethodNotAllowedException() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("POST");

        ProblemDetail result = advice.handleMainException(exception);

        assertThat(result.getStatus()).isEqualTo(405); // METHOD_NOT_ALLOWED
    }

    // ----------------------------
    // SystemException tests
    // ----------------------------
    @Test
    @DisplayName("handleSystemException returns correct status and title")
    void testHandleSystemExceptionValidStatus() {
        SystemException exception = new SystemException("System failure", "SYS_ERROR", 400);

        ProblemDetail result = advice.handleSystemException(exception);

        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getTitle()).isEqualTo("SYS_ERROR");
        assertThat(result.getDetail()).isEqualTo("System failure");

        verify(auditionLogger).logErrorWithException(any(), eq("SystemException caught"), eq(exception));
        verify(auditionLogger).logStandardProblemDetail(any(), any(ProblemDetail.class), eq(exception));
    }

    @Test
    @DisplayName("handleSystemException falls back to INTERNAL_SERVER_ERROR for invalid status")
    void testHandleSystemExceptionInvalidStatus() {
        SystemException exception = new SystemException("System failure", "SYS_ERROR", 9999);

        ProblemDetail result = advice.handleSystemException(exception);

        assertThat(result.getStatus()).isEqualTo(500);
        assertThat(result.getTitle()).isEqualTo("SYS_ERROR");

        verify(auditionLogger).logHttpStatusCodeError(any(), contains("Error Code from Exception"), eq(9999));
    }

    // ----------------------------
    // ProblemDetail logging edge case
    // ----------------------------
    @Test
    @DisplayName("createProblemDetail logs ProblemDetail even with empty exception")
    void testProblemDetailLoggingEdgeCase() {
        Exception exception = new Exception();

        ProblemDetail result = advice.handleMainException(exception);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(500);

        verify(auditionLogger).logStandardProblemDetail(any(), any(ProblemDetail.class), eq(exception));
    }
}
