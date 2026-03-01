package com.audition.common.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

import static org.mockito.Mockito.*;

public class AuditionLoggerTest {
    private AuditionLogger auditionLogger;
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        auditionLogger = new AuditionLogger();
        mockLogger = mock(Logger.class);
    }

    // -----------------------------
    // info(String)
    // -----------------------------
    @Test
    @DisplayName("info logs message when info enabled")
    void testInfoEnabled() {
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        auditionLogger.info(mockLogger, "Test message");
        verify(mockLogger).info("Test message");
    }

    @Test
    @DisplayName("info does not log message when info disabled")
    void testInfoDisabled() {
        when(mockLogger.isInfoEnabled()).thenReturn(false);
        auditionLogger.info(mockLogger, "Test message");
        verify(mockLogger, never()).info(anyString());
    }

    // -----------------------------
    // info(String, Object)
    // -----------------------------
    @Test
    @DisplayName("info with object logs when info enabled")
    void testInfoWithObjectEnabled() {
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        auditionLogger.info(mockLogger, "Test {}", "OBJECT");
        verify(mockLogger).info("Test {}", "OBJECT");
    }

    @Test
    @DisplayName("info with object does not log when info disabled")
    void testInfoWithObjectDisabled() {
        when(mockLogger.isInfoEnabled()).thenReturn(false);
        auditionLogger.info(mockLogger, "Test {}", "OBJECT");
        verify(mockLogger, never()).info(anyString(), (Throwable) any());
    }

    // -----------------------------
    // debug(String)
    // -----------------------------
    @Test
    @DisplayName("debug logs message when debug enabled")
    void testDebugEnabled() {
        when(mockLogger.isDebugEnabled()).thenReturn(true);
        auditionLogger.debug(mockLogger, "Debug message");
        verify(mockLogger).debug("Debug message");
    }

    @Test
    @DisplayName("debug does not log message when debug disabled")
    void testDebugDisabled() {
        when(mockLogger.isDebugEnabled()).thenReturn(false);
        auditionLogger.debug(mockLogger, "Debug message");
        verify(mockLogger, never()).debug(anyString());
    }

    // -----------------------------
    // warn(String)
    // -----------------------------
    @Test
    @DisplayName("warn logs message when warn enabled")
    void testWarnEnabled() {
        when(mockLogger.isWarnEnabled()).thenReturn(true);
        auditionLogger.warn(mockLogger, "Warn message");
        verify(mockLogger).warn("Warn message");
    }

    @Test
    @DisplayName("warn does not log message when warn disabled")
    void testWarnDisabled() {
        when(mockLogger.isWarnEnabled()).thenReturn(false);
        auditionLogger.warn(mockLogger, "Warn message");
        verify(mockLogger, never()).warn(anyString());
    }

    // -----------------------------
    // error(String)
    // -----------------------------
    @Test
    @DisplayName("error logs message when error enabled")
    void testErrorEnabled() {
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        auditionLogger.error(mockLogger, "Error message");
        verify(mockLogger).error("Error message");
    }

    @Test
    @DisplayName("error does not log message when error disabled")
    void testErrorDisabled() {
        when(mockLogger.isErrorEnabled()).thenReturn(false);
        auditionLogger.error(mockLogger, "Error message");
        verify(mockLogger, never()).error(anyString());
    }

    // -----------------------------
    // logErrorWithException
    // -----------------------------
    @Test
    @DisplayName("logErrorWithException logs exception when error enabled")
    void testLogErrorWithExceptionEnabled() {
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        Exception ex = new RuntimeException("Cause");
        auditionLogger.logErrorWithException(mockLogger, "Error occurred", ex);
        verify(mockLogger).error("Error occurred", ex);
    }

    @Test
    @DisplayName("logErrorWithException does not log when error disabled")
    void testLogErrorWithExceptionDisabled() {
        when(mockLogger.isErrorEnabled()).thenReturn(false);
        Exception ex = new RuntimeException();
        auditionLogger.logErrorWithException(mockLogger, "Error occurred", ex);
        verify(mockLogger, never()).error(anyString(), (Throwable) any());
    }

    // -----------------------------
    // logStandardProblemDetail
    // -----------------------------
    @Test
    @DisplayName("logStandardProblemDetail logs formatted message when error enabled")
    void testLogStandardProblemDetailEnabled() {
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatusCode.valueOf(500));
        problem.setTitle("TestTitle");
        problem.setDetail("TestDetail");
        Exception ex = new RuntimeException("Cause");

        auditionLogger.logStandardProblemDetail(mockLogger, problem, ex);

        String expectedMessage = "ProblemDetail [status=500, title=TestTitle, detail=TestDetail]";
        verify(mockLogger).error(expectedMessage, ex);
    }

    @Test
    @DisplayName("logStandardProblemDetail does not log when error disabled")
    void testLogStandardProblemDetailDisabled() {
        when(mockLogger.isErrorEnabled()).thenReturn(false);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatusCode.valueOf(500));
        Exception ex = new RuntimeException("Cause");
        auditionLogger.logStandardProblemDetail(mockLogger, problem, ex);
        verify(mockLogger, never()).error(anyString(), (Throwable) any());
    }

    // -----------------------------
    // logHttpStatusCodeError
    // -----------------------------
    @Test
    @DisplayName("logHttpStatusCodeError logs formatted message when error enabled")
    void testLogHttpStatusCodeErrorEnabled() {
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        auditionLogger.logHttpStatusCodeError(mockLogger, "Not Found", 404);
        String expectedMessage = "HTTP Error [404]: Not Found\n";
        verify(mockLogger).error(expectedMessage);
    }

    @Test
    @DisplayName("logHttpStatusCodeError does not log when error disabled")
    void testLogHttpStatusCodeErrorDisabled() {
        when(mockLogger.isErrorEnabled()).thenReturn(false);
        auditionLogger.logHttpStatusCodeError(mockLogger, "Not Found", 404);
        verify(mockLogger, never()).error(anyString());
    }
}
