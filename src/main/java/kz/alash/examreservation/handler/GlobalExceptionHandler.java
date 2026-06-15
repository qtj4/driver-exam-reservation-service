package kz.alash.examreservation.handler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;
import kz.alash.examreservation.dto.response.ErrorResponse;
import kz.alash.examreservation.exception.BusinessException;
import kz.alash.examreservation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    private final MessageSource messageSource;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException exception) {
        return response("NOT_FOUND", HttpStatus.NOT_FOUND, exception.getMessage(), null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException exception) {
        return response("BUSINESS_RULE_VIOLATION", HttpStatus.BAD_REQUEST, exception.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return response(VALIDATION_ERROR, HttpStatus.BAD_REQUEST, message("validation.failed"), fieldErrors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getParameterValidationResults().forEach(result -> {
            String field = result.getMethodParameter().getParameterName();
            String message = result.getResolvableErrors().stream()
                    .findFirst()
                    .map(this::message)
                    .orElse(message("validation.failed"));
            fieldErrors.put(field == null ? "request" : field, message);
        });

        return response(VALIDATION_ERROR, HttpStatus.BAD_REQUEST, message("validation.failed"), fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                fieldErrors.put(extractFieldName(violation.getPropertyPath().toString()), violation.getMessage())
        );

        return response(VALIDATION_ERROR, HttpStatus.BAD_REQUEST, message("validation.failed"), fieldErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        Map<String, String> fieldErrors = Map.of(exception.getName(), message("validation.failed"));
        return response(VALIDATION_ERROR, HttpStatus.BAD_REQUEST, message("validation.failed"), fieldErrors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException exception) {
        Map<String, String> fieldErrors = Map.of(exception.getParameterName(), message("validation.failed"));
        return response(VALIDATION_ERROR, HttpStatus.BAD_REQUEST, message("validation.failed"), fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        return response(VALIDATION_ERROR, HttpStatus.BAD_REQUEST, message("validation.failed"), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return response("DATABASE_ERROR", HttpStatus.CONFLICT, message("database.error"), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("Unexpected error", exception);
        return response("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, message("error.unexpected"), null);
    }

    private ResponseEntity<ErrorResponse> response(
            String error,
            HttpStatus status,
            String message,
            Map<String, String> fieldErrors
    ) {
        ErrorResponse response = ErrorResponse.builder()
                .error(error)
                .status(status.value())
                .message(message)
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(response);
    }

    private String message(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, null, code, locale);
    }

    private String message(MessageSourceResolvable resolvable) {
        return messageSource.getMessage(resolvable, LocaleContextHolder.getLocale());
    }

    private String extractFieldName(String propertyPath) {
        int dotIndex = propertyPath.lastIndexOf('.');
        if (dotIndex < 0) {
            return propertyPath;
        }
        return propertyPath.substring(dotIndex + 1);
    }
}
