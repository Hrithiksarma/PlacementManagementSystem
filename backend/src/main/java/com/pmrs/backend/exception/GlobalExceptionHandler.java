package com.pmrs.backend.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Domain exceptions ─────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(EligibilityException.class)
    public ResponseEntity<Map<String, Object>> handleEligibility(EligibilityException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── Validation exceptions ─────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String violations = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        Map<String, Object> body = body(HttpStatus.BAD_REQUEST, violations);
        // keep field-level detail for forms
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        body.put("errors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String violations = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.UNPROCESSABLE_ENTITY,
                "Validation failed: " + (violations.isBlank() ? ex.getMessage() : violations));
    }

    // ── Database / transaction exceptions ─────────────────────────────────────

    /**
     * MySQL triggers fire at execute-statement time (not at commit), so Spring wraps
     * them in JpaSystemException rather than DataIntegrityViolationException.
     */
    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<Map<String, Object>> handleJpaSystem(JpaSystemException ex) {
        String raw = extractTriggerMessage(ex);
        if (raw.startsWith("INELIGIBLE:")) {
            return error(HttpStatus.BAD_REQUEST, raw.substring("INELIGIBLE:".length()).trim());
        }
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String raw = extractTriggerMessage(ex);
        if (raw.startsWith("INELIGIBLE:")) {
            // Strip prefix so the frontend can show the message directly
            return error(HttpStatus.BAD_REQUEST, raw.substring("INELIGIBLE:".length()).trim());
        }
        return error(HttpStatus.UNPROCESSABLE_ENTITY, raw);
    }

    /**
     * Catches DataIntegrityViolationException (and ConstraintViolationException) that are
     * thrown during transaction commit, which Spring wraps in TransactionSystemException
     * before they reach the controller's try/catch.
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionSystem(TransactionSystemException ex) {
        Throwable root = ex.getRootCause();
        if (root instanceof ConstraintViolationException cve) {
            return handleConstraintViolation(cve);
        }
        if (root instanceof DataIntegrityViolationException dive) {
            return handleDataIntegrity(dive);
        }
        String msg = root != null ? root.getMessage() : ex.getMessage();
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "Transaction failed: " + (msg != null ? msg : "unknown reason"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArg(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── Catch-all ─────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        ex.printStackTrace();
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getClass().getSimpleName() + ": " + ex.getMessage());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(body(status, message));
    }

    private static Map<String, Object> body(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("message", message);   // primary key the frontend reads
        body.put("error", message);     // kept for backwards-compat
        return body;
    }

    private static String extractTriggerMessage(Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            String msg = cause.getMessage();
            if (msg != null && msg.contains("INELIGIBLE:")) {
                int start = msg.indexOf("INELIGIBLE:");
                String raw = msg.substring(start);
                // Hibernate appends "] [<SQL statement>]" — cut there first
                int cut = raw.indexOf("] [");
                if (cut != -1) raw = raw.substring(0, cut);
                // Also stop at any newline
                int nl = raw.indexOf('\n');
                if (nl != -1) raw = raw.substring(0, nl);
                // Strip a stray trailing ]
                if (raw.endsWith("]")) raw = raw.substring(0, raw.length() - 1);
                return raw.trim();
            }
            cause = cause.getCause();
        }
        return "Operation rejected by a database policy rule.";
    }
}
