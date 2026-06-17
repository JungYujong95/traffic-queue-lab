package io.github.dbwhd5566.trafficqueuelab.global.exception;

import java.sql.SQLTransientConnectionException;
import java.util.List;
import java.util.Objects;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        return handleFieldErrors(exception.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException exception) {
        return handleFieldErrors(exception.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException() {
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST_BODY));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception
    ) {
        String message = "%s 파라미터는 필수입니다.".formatted(exception.getParameterName());
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorCode.MISSING_REQUIRED_PARAMETER, message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception
    ) {
        String message = "%s 파라미터 타입이 올바르지 않습니다.".formatted(exception.getName());
        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH, message));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException() {
        return ResponseEntity
                .status(ErrorCode.NOT_FOUND.getStatus())
                .body(ErrorResponse.of(ErrorCode.NOT_FOUND));
    }

    @ExceptionHandler({
            CannotGetJdbcConnectionException.class,
            CannotCreateTransactionException.class,
            JDBCConnectionException.class
    })
    public ResponseEntity<ErrorResponse> handleDbConnectionException(Exception exception) {
        if (!isConnectionTimeout(exception)) {
            return handleException();
        }

        return ResponseEntity
                .status(ErrorCode.DB_CONNECTION_TIMEOUT.getStatus())
                .body(ErrorResponse.of(ErrorCode.DB_CONNECTION_TIMEOUT));
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleErrorResponseException(ErrorResponseException exception) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(exception.getStatusCode())
                .body(ErrorResponse.of(errorCode, exception.getBody().getDetail()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException() {
        return ResponseEntity
                .internalServerError()
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    private ResponseEntity<ErrorResponse> handleFieldErrors(List<FieldError> fieldErrors) {
        List<FieldErrorDetail> errors = fieldErrors.stream()
                .map(this::toFieldErrorDetail)
                .toList();

        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, errors));
    }

    private FieldErrorDetail toFieldErrorDetail(FieldError fieldError) {
        return new FieldErrorDetail(
                fieldError.getField(),
                Objects.toString(fieldError.getRejectedValue(), null),
                fieldError.getDefaultMessage()
        );
    }

    private boolean isConnectionTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof CannotGetJdbcConnectionException
                    || current instanceof JDBCConnectionException
                    || current instanceof SQLTransientConnectionException) {
                return true;
            }

            String message = current.getMessage();
            if (message != null && message.contains("Connection is not available")) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}
