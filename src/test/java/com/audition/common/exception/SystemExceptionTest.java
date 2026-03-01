package com.audition.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemExceptionTest {

    @Test
    @DisplayName("Default constructor sets no message, title or statusCode")
    void testDefaultConstructor() {
        SystemException ex = new SystemException();

        assertThat(ex.getMessage()).isNull();
        assertThat(ex.getTitle()).isNull();
        assertThat(ex.getStatusCode()).isNull();
        assertThat(ex.getDetail()).isNull();
    }

    @Test
    @DisplayName("Constructor with message sets message and default title")
    void testConstructorWithMessage() {
        SystemException ex = new SystemException("Something went wrong");

        assertThat(ex.getMessage()).isEqualTo("Something went wrong");
        assertThat(ex.getTitle()).isEqualTo(SystemException.DEFAULT_TITLE);
        assertThat(ex.getStatusCode()).isNull();
        assertThat(ex.getDetail()).isNull();
    }

    @Test
    @DisplayName("Constructor with message and errorCode sets statusCode and default title")
    void testConstructorWithMessageAndCode() {
        SystemException ex = new SystemException("Error occurred", 400);

        assertThat(ex.getMessage()).isEqualTo("Error occurred");
        assertThat(ex.getTitle()).isEqualTo(SystemException.DEFAULT_TITLE);
        assertThat(ex.getStatusCode()).isEqualTo(400);
        assertThat(ex.getDetail()).isNull();
    }

    @Test
    @DisplayName("Constructor with message and Throwable sets cause and default title")
    void testConstructorWithMessageAndThrowable() {
        Throwable cause = new RuntimeException("Cause");
        SystemException ex = new SystemException("Error with cause", cause);

        assertThat(ex.getMessage()).isEqualTo("Error with cause");
        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getTitle()).isEqualTo(SystemException.DEFAULT_TITLE);
        assertThat(ex.getStatusCode()).isNull();
        assertThat(ex.getDetail()).isNull();
    }

    @Test
    @DisplayName("Constructor with detail, title, and statusCode sets all fields correctly")
    void testConstructorWithDetailTitleCode() {
        SystemException ex = new SystemException("Detail message", "Custom Title", 401);

        assertThat(ex.getMessage()).isEqualTo("Detail message");
        assertThat(ex.getDetail()).isEqualTo("Detail message");
        assertThat(ex.getTitle()).isEqualTo("Custom Title");
        assertThat(ex.getStatusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("Constructor with detail, title, and Throwable sets all fields and default statusCode 500")
    void testConstructorWithDetailTitleThrowable() {
        Throwable cause = new RuntimeException("Cause");
        SystemException ex = new SystemException("Detail with cause", "Custom Title", cause);

        assertThat(ex.getMessage()).isEqualTo("Detail with cause");
        assertThat(ex.getDetail()).isEqualTo("Detail with cause");
        assertThat(ex.getTitle()).isEqualTo("Custom Title");
        assertThat(ex.getStatusCode()).isEqualTo(500);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Constructor with detail, errorCode, and Throwable sets all fields with default title")
    void testConstructorWithDetailCodeThrowable() {
        Throwable cause = new RuntimeException("Cause");
        SystemException ex = new SystemException("Detail message", 402, cause);

        assertThat(ex.getMessage()).isEqualTo("Detail message");
        assertThat(ex.getDetail()).isEqualTo("Detail message");
        assertThat(ex.getTitle()).isEqualTo(SystemException.DEFAULT_TITLE);
        assertThat(ex.getStatusCode()).isEqualTo(402);
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("Constructor with detail, title, errorCode, and Throwable sets all fields")
    void testConstructorWithAllParams() {
        Throwable cause = new RuntimeException("Cause");
        SystemException ex = new SystemException("Detail message", "Custom Title", 403, cause);

        assertThat(ex.getMessage()).isEqualTo("Detail message");
        assertThat(ex.getDetail()).isEqualTo("Detail message");
        assertThat(ex.getTitle()).isEqualTo("Custom Title");
        assertThat(ex.getStatusCode()).isEqualTo(403);
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
