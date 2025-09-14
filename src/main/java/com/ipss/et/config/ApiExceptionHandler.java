package com.ipss.et.config;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handler DEV: no oculta el error.
 * Devuelve mensaje y rootCause para poder arreglar el 500 rápido.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private Map<String, Object> body(HttpStatus status, String message, String root, String exClass, String path) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", Instant.now().toString());
        map.put("status", status.value());
        map.put("error", status.getReasonPhrase());
        map.put("message", message);
        map.put("rootCause", root);
        map.put("exception", exClass);
        map.put("path", path);
        return map;
    }

    private static String rootMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getAllErrors().stream()
                .findFirst().map(e -> e.getDefaultMessage()).orElse("Validación inválida");
        log.warn("400 @ {} -> {}", req.getRequestURI(), msg);
        return ResponseEntity.badRequest()
                .body(body(HttpStatus.BAD_REQUEST, msg, rootMessage(ex), ex.getClass().getName(), req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex, HttpServletRequest req) {
        // Log completo al server
        log.error("500 @ {} -> {}", req.getRequestURI(), ex.getMessage(), ex);
        // Respuesta con detalle y root cause
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage(),
                        rootMessage(ex),
                        ex.getClass().getName(),
                        req.getRequestURI()));
    }
}
