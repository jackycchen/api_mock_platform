package com.apimock.exception;

import com.apimock.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        return buildErrorResponse(ErrorCode.AUTHENTICATION_FAILED, e.getMessage(), null);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleAccountLockedException(AccountLockedException e) {
        Map<String, Object> data = new HashMap<>();
        data.put("lockedUntil", e.getLockedUntil());
        return buildErrorResponse(ErrorCode.ACCOUNT_LOCKED, e.getMessage(), data);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        Map<String, String> data = new HashMap<>();
        data.put("field", e.getField());
        return buildErrorResponse(ErrorCode.CONFLICT_ERROR, e.getMessage(), data);
    }

    @ExceptionHandler(PasswordValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handlePasswordValidationException(PasswordValidationException e) {
        return buildErrorResponse(ErrorCode.VALIDATION_ERROR, e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return buildErrorResponse(ErrorCode.VALIDATION_ERROR, "参数校验失败", errors);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(BindException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return buildErrorResponse(ErrorCode.VALIDATION_ERROR, "参数绑定失败", errors);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProjectNotFoundException(ProjectNotFoundException e) {
        return buildErrorResponse(ErrorCode.PROJECT_NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(ProjectNameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleProjectNameAlreadyExistsException(ProjectNameAlreadyExistsException e) {
        return buildErrorResponse(ErrorCode.CONFLICT_ERROR, e.getMessage(), null);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestException(InvalidRequestException e) {
        return buildErrorResponse(ErrorCode.VALIDATION_ERROR, e.getMessage(), null);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedAccessException(UnauthorizedAccessException e) {
        return buildErrorResponse(ErrorCode.ACCESS_DENIED, e.getMessage(), null);
    }

    @ExceptionHandler(ApiNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiNotFoundException(ApiNotFoundException e) {
        return buildErrorResponse(ErrorCode.API_NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(ApiPathConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiPathConflictException(ApiPathConflictException e) {
        return buildErrorResponse(ErrorCode.CONFLICT_ERROR, e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        LOGGER.error("Unhandled exception", e);
        return buildErrorResponse(ErrorCode.INTERNAL_ERROR, null, null);
    }

    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(ErrorCode errorCode, String message, T data) {
        String responseMessage = (message == null || message.isBlank()) ? errorCode.getDefaultMessage() : message;
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode, responseMessage, data));
    }
}
