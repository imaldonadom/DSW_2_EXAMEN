package com.ipss.et.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> onRse(ResponseStatusException ex) {
        HttpStatusCode code = ex.getStatusCode();
        HttpStatus statusEnum = HttpStatus.resolve(code.value());
        String reason = (statusEnum != null) ? statusEnum.getReasonPhrase() : "Error";

        return ResponseEntity.status(code).body(
                Map.of(
                        "status", code.value(),
                        "error", reason,
                        "message", ex.getReason()
                )
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> onFk(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of(
                        "status", 409,
                        "error", "Conflict",
                        "message", "No se puede eliminar: el álbum tiene datos relacionados"
                )
        );
    }
}
