package com.audition.web.advice;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    public static final String DEFAULT_TITLE = "API Error Occurred";
    public static final String DEFAULT_MESSAGE = "API Error occurred. Please contact support or administrator.";
    private static final String ERROR_MESSAGE = " Error Code from Exception could not be mapped to a valid HttpStatus Code - ";

    private final AuditionLogger auditionLogger;
    private final Logger logger;

    @Autowired
    public ExceptionControllerAdvice(AuditionLogger auditionLogger) {
        this.auditionLogger = auditionLogger;
        this.logger = LoggerFactory.getLogger(ExceptionControllerAdvice.class);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ProblemDetail handleHttpClientException(final HttpClientErrorException e) {
        auditionLogger.logErrorWithException(logger, "HttpClientErrorException caught", e);
        return createProblemDetail(e, e.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleMainException(final Exception e) {
        auditionLogger.logErrorWithException(logger, "Unhandled Exception caught", e);
        HttpStatusCode status = getHttpStatusCodeFromException(e);
        return createProblemDetail(e, status);

    }

    @ExceptionHandler(SystemException.class)
    ProblemDetail handleSystemException(final SystemException e) {
        auditionLogger.logErrorWithException(logger, "SystemException caught", e);
        HttpStatusCode status = getHttpStatusCodeFromSystemException(e);
        return createProblemDetail(e, status);

    }

    private ProblemDetail createProblemDetail(final Exception exception, final HttpStatusCode statusCode) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(statusCode);
        problemDetail.setDetail(getMessageFromException(exception));
        problemDetail.setTitle(exception instanceof SystemException
                ? ((SystemException) exception).getTitle()
                : DEFAULT_TITLE);

        // Log the standardized ProblemDetail
        auditionLogger.logStandardProblemDetail(logger, problemDetail, exception);

        return problemDetail;
    }

    private String getMessageFromException(final Exception exception) {
        return StringUtils.isNotBlank(exception.getMessage()) ? exception.getMessage() : DEFAULT_MESSAGE;
    }

    private HttpStatusCode getHttpStatusCodeFromSystemException(final SystemException exception) {
        try {
            return HttpStatusCode.valueOf(exception.getStatusCode());
        } catch (IllegalArgumentException iae) {
            auditionLogger.logHttpStatusCodeError(logger, ERROR_MESSAGE, exception.getStatusCode());
            return INTERNAL_SERVER_ERROR;
        }
    }

    private HttpStatusCode getHttpStatusCodeFromException(final Exception exception) {
        if (exception instanceof HttpClientErrorException clientError) {
            return clientError.getStatusCode();
        } else if (exception instanceof org.springframework.web.HttpRequestMethodNotSupportedException
                || exception instanceof org.springframework.web.server.MethodNotAllowedException) {
            return METHOD_NOT_ALLOWED;
        }
        return INTERNAL_SERVER_ERROR;
    }
}



