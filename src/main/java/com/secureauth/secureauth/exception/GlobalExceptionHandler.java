package com.secureauth.secureauth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex) {
        log.error("Auth error: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), ex.getStatus(), ex.getErrorCode());
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLockedException(
            AccountLockedException ex) {
        log.error("Account locked: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), ex.getStatus(), ex.getErrorCode());
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ErrorResponse> handleTokenException(TokenException ex) {
        log.error("Token error: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), ex.getStatus(), ex.getErrorCode());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), ex.getStatus(), ex.getErrorCode());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.error("Business error: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), ex.getStatus(), ex.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("VALIDATION_ERROR")
                .message("Doğrulama hatası")
                .validationErrors(errors)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex) {
        return buildErrorResponse("Kullanıcı adı veya şifre hatalı.",
                HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(LockedException ex) {
        return buildErrorResponse("Hesabınız kilitlenmiştir.",
                HttpStatus.LOCKED, "ACCOUNT_LOCKED");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex) {
        return buildErrorResponse("Hesabınız devre dışıdır.",
                HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex) {
        return buildErrorResponse("Bu işlem için yetkiniz bulunmamaktadır.",
                HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return buildErrorResponse("Beklenmeyen bir hata oluştu.",
                HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            String message, HttpStatus status, String errorCode) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .errorCode(errorCode)
                .message(message)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}