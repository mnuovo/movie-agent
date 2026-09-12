package com.michelenuovo.movieagent.advice.error;

import com.michelenuovo.movieagent.config.CorrelationIdWebFilter;
import com.michelenuovo.movieagent.dto.error.ApiErrorResponse;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.bind.support.WebExchangeBindException;

/**
 * Centralized exception mapping for JSON API error responses.
 */
@RestControllerAdvice(basePackages = "com.michelenuovo.movieagent")
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String GENERIC_SERVER_ERROR_MESSAGE = "An unexpected error occurred.";
    private static final String DEFAULT_REQUEST_FAILED_MESSAGE = "Request failed.";

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NoSuchElementException exception, ServerWebExchange exchange) {
        logger.warn("Resource not found: {}", exception.getMessage());
        return buildResponse(exchange, HttpStatus.NOT_FOUND, sanitizeClientMessage(exception.getMessage()), List.of());
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            ServerWebInputException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, ServerWebExchange exchange) {
        logger.warn("Bad request: {}", exception.getMessage());
        return buildResponse(exchange, HttpStatus.BAD_REQUEST, sanitizeClientMessage(exception.getMessage()), List.of());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiErrorResponse> handleWebExchangeBind(WebExchangeBindException exception, ServerWebExchange exchange) {
        logger.warn("Request validation failed: {}", exception.getMessage());
        List<ApiErrorResponse.FieldViolation> violations = exception.getFieldErrors().stream()
                .map(error -> new ApiErrorResponse.FieldViolation(error.getField(), defaultMessage(error.getDefaultMessage())))
                .toList();
        return buildResponse(exchange, HttpStatus.BAD_REQUEST, "Validation failed.", violations);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, ServerWebExchange exchange) {
        logger.warn("Method argument validation failed: {}", exception.getMessage());
        List<ApiErrorResponse.FieldViolation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiErrorResponse.FieldViolation(error.getField(), defaultMessage(error.getDefaultMessage())))
                .toList();
        return buildResponse(exchange, HttpStatus.BAD_REQUEST, "Validation failed.", violations);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleUpstream(WebClientResponseException exception, ServerWebExchange exchange) {
        HttpStatusCode statusCode = exception.getStatusCode();
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        HttpStatus resolvedStatus = status != null ? status : HttpStatus.BAD_GATEWAY;
        logger.warn("Upstream call failed: status={} message={}", statusCode.value(), exception.getMessage());
        return buildResponse(exchange, resolvedStatus, "Upstream service request failed.", List.of());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException exception, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        HttpStatus resolvedStatus = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        logger.warn("Request failed with response status: status={} reason={}", resolvedStatus.value(), exception.getReason());
        return buildResponse(exchange, resolvedStatus, defaultMessage(exception.getReason()), List.of());
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ApiErrorResponse> handleErrorResponse(ErrorResponseException exception, ServerWebExchange exchange) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        HttpStatus resolvedStatus = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        logger.warn("Framework error response: status={} detail={}", resolvedStatus.value(), exception.getMessage());
        return buildResponse(exchange, resolvedStatus, DEFAULT_REQUEST_FAILED_MESSAGE, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, ServerWebExchange exchange) {
        logger.error("Unhandled exception while processing request.", exception);
        return buildResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_SERVER_ERROR_MESSAGE, List.of());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            ServerWebExchange exchange,
            HttpStatus status,
            String message,
            List<ApiErrorResponse.FieldViolation> violations
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value(),
                resolveCorrelationId(exchange),
                violations
        );
        return ResponseEntity.status(status).body(body);
    }

    private String resolveCorrelationId(ServerWebExchange exchange) {
        String fromResponse = exchange.getResponse().getHeaders().getFirst(CorrelationIdWebFilter.CORRELATION_ID_HEADER);
        if (fromResponse != null && !fromResponse.isBlank()) {
            return fromResponse;
        }
        return exchange.getRequest().getHeaders().getFirst(CorrelationIdWebFilter.CORRELATION_ID_HEADER);
    }

    private String sanitizeClientMessage(String message) {
        return Optional.ofNullable(message)
                .filter(value -> !value.isBlank())
                .map(this::defaultMessage)
                .orElse(DEFAULT_REQUEST_FAILED_MESSAGE);
    }

    private String defaultMessage(String message) {
        return message == null || message.isBlank() ? DEFAULT_REQUEST_FAILED_MESSAGE : message;
    }
}




