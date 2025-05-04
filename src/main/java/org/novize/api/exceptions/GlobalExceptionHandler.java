package org.novize.api.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.xml.bind.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.security.SignatureException;
import java.util.*;

/**
 * GlobalExceptionHandler is a centralized exception handler for a Spring RESTful application.
 * It intercepts exceptions thrown by the controller methods and returns a unified error response.
 * The class uses the `@RestControllerAdvice` annotation provided by Spring to apply the
 * exception handling globally across the entire application.
 *
 * The handler processes various types of exceptions and responds with an appropriate
 * error message, HTTP status codes, and additional details wherever applicable.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles UserNotFoundException and returns a 404 Not Found response.
     *
     * @param ex The exception that was thrown.
     * @return An ErrorMessage object containing error details.
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleNotFoundException(UserNotFoundException ex) {
        List<String> errors = Collections.singletonList(ex.getMessage());
        return ErrorMessage.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(new Date())
                .message("User not found")
                .description(getErrorsMap(errors).toString())
                .build();
    }

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorsMap = new HashMap<>();
        errorsMap.put("errors", errors);
        return errorsMap;
    }

    /**
     * Handles JWTValidationException and returns a 401 Unauthorized response.
     */
    @ExceptionHandler({SignatureException.class, MalformedJwtException.class, JwtException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorMessage handleJWTValidationException(RuntimeException ex) {
        return ErrorMessage
                .builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .timestamp(new Date())
                .message("Unauthorized")
                .description(ex.getLocalizedMessage())
                .build();
    }

    /**
     * Handles EntityNotFoundException and returns a 404 Not Found response.
     *
     * @param ex The exception that was thrown.
     * @return An ErrorMessage object containing error details.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleEntityNotFoundException(EntityNotFoundException ex) {
        return ErrorMessage
                .builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(new Date())
                .message(ex.getMessage())
                .description(ex.getLocalizedMessage())
                .build();
    }

    /**
     * Handles IllegalArgumentException and returns a 400 Bad Request response.
     *
     * @param ex The exception that was thrown.
     * @return An ErrorMessage object containing error details.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleIllegalArgumentException(IllegalArgumentException ex) {
        return ErrorMessage
                .builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(new Date())
                .message(ex.getMessage())
                .description(ex.getLocalizedMessage())
                .build();
    }

    /**
     * Handles InvalidRequestException and returns a 400 Bad Request response.
     *
     * @param ex The exception that was thrown.
     * @return An ErrorMessage object containing error details.
     */
    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleInvalidRequestException(InvalidRequestException ex) {
        return ErrorMessage
                .builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(new Date())
                .message(ex.getMessage())
                .description(ex.getLocalizedMessage())
                .build();
    }


    /**
     * Handles MethodArgumentNotValidException and returns a 400 Bad Request response.
     *
     * @param ex The exception that was thrown.
     * @return An ErrorMessage object containing error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ErrorMessage
                .builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(new Date())
                .message("Validation failed")
                .description(errors.toString())
                .build();
    }

    /**
     * Handles TokenRefreshException and returns a 403 Forbidden response.
     *
     * @param ex The exception that was thrown.
     * @param request The web request that caused the exception.
     * @return An ErrorMessage object containing error details.
     */
    @ExceptionHandler(value = TokenRefreshException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage handleTokenRefreshException(TokenRefreshException ex, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.FORBIDDEN.value(),
                new Date(),
                ex.getMessage(),
                request.getDescription(false));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorMessage handleExpiredJwtException(ExpiredJwtException ex) {
        return ErrorMessage
                .builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .timestamp(new Date())
                .message("Token expired")
                .description(ex.getLocalizedMessage())
                .build();
    }

    /**
     * Handles all other exceptions and returns a 500 Internal Server Error response.
     *
     * @param ex The exception that was thrown.
     * @return An ErrorMessage object containing error details.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleGeneralException(Exception ex) {
        return ErrorMessage
                .builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(new Date())
                .message(ex.getMessage())
                .description(ex.getLocalizedMessage())
                .build();
    }
}