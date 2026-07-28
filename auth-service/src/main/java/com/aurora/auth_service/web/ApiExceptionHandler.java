package com.aurora.auth_service.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ApiExceptionHandler {

    // AuthService içinden fırlattığımız RuntimeException'ları yakalar
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeExceptions(RuntimeException ex) {

        String message = ex.getMessage();

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if ("email_taken".equals(message)) {
            status = HttpStatus.CONFLICT;  //409
        } else if ("invalid_credentials".equals(message)) {
            status = HttpStatus.UNAUTHORIZED; // 401
        }
        return ResponseEntity.status(status).body(Map.of("error", message != null ? message : "internal"));
    }

    // @Valid anotasyonundan dönen (örneğin e-posta formata uymazsa) hataları yakalar
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Spec dosyası geçersiz istekler için her zaman 422 döner diyor
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "invalid_request"));
    }

}
