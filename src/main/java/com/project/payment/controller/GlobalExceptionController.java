package com.project.payment.controller;

import com.project.payment.dto.response.ApiResponse;
import com.project.payment.exception.*;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MethodNotAllowedException;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleAccountExistException(UserNotFoundException ex) {
        log.error("account exist exception occurred: {}", ex.getMessage());

        var response = ApiResponse.error(ex.getLocalizedMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorizedException(UnauthorizedException ex) {
        log.error("unauthorized exception occurred: {}", ex.getMessage());

        var response = ApiResponse.error(ex.getLocalizedMessage());
        return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequestException(BadRequestException ex) {
        log.error("bad request exception occurred: {}", ex.getMessage());

        var response = ApiResponse.error(ex.getLocalizedMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(QrCodeGenerationException.class)
    public ResponseEntity<ApiResponse<?>> handleQrCodeGenerationException(QrCodeGenerationException ex) {
        log.error("qr code generation exception occurred: {}", ex.getMessage());

        var response = ApiResponse.error(ex.getLocalizedMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {
        log.info("uncaught exception occurred: {}", ex.getMessage(),ex);

        var response = ApiResponse.error(ex.getLocalizedMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        log.info("access denied exception occurred: {}", ex.getMessage(),ex);

        var response = ApiResponse.error(ex.getLocalizedMessage());
        return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException ex) {
        log.info("access denied exception occurred: {}", ex.getMessage(),ex);

        var response = ApiResponse.error("kindly login first");
        return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
    }




    @ExceptionHandler(GenericException.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(GenericException ex) {
        log.info("generic exception occurred: {}", ex.getMessage());

        var response = ApiResponse.error(ex.getLocalizedMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodNotAllowedException(MethodNotAllowedException ex) {

        var response = ApiResponse.error(ex.getLocalizedMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {

        var response = ApiResponse.error("MISSING OR INVALID HEADER/AUTHENTICATION VALUES");
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(ConstraintViolationException ex) {

        var response = ApiResponse.error(ex.getLocalizedMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.debug("Field validation exception occurred: {}", ex.getFieldErrors());

        // Collect field errors and join them into a single string
        String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(","));


        var response = ApiResponse.error(fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<?>> handleSQLException(SQLException ex) {
        if (ex.getErrorCode() == 1062) { // MySQL duplicate entry error code
            var response = ApiResponse.error("constraint violation");
            return ResponseEntity.internalServerError().body(response);
        }
        var response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BatchUpdateException.class)
    public ResponseEntity<ApiResponse<?>> handleBatchUpdateException(BatchUpdateException ex) {

        var response = ApiResponse.error(ex.getLocalizedMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ApiResponse<?>> handlePersistenceException(PersistenceException ex) {

        var response = ApiResponse.error(ex.getLocalizedMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {

        var response = ApiResponse.error(ex.getLocalizedMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<?>> handleDataAccessException(DataAccessException ex) {
        log.info("Database error occurred: {}", ex.getMessage(), ex);

        String errorMessage = (ex.getRootCause() instanceof SQLTimeoutException)
                ? "Database connection timed out"
                : "Error accessing database";

        var response = ApiResponse.error(errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(org.hibernate.exception.ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(org.hibernate.exception.ConstraintViolationException ex) {

        var response = ApiResponse.error(ex.getLocalizedMessage());
        return ResponseEntity.internalServerError().body(response);
    }
}
