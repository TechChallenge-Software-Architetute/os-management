package com.os.features.product.exception;

import com.os.features.client.exception.ClientNotFoundException;
import com.os.features.vehicle.exception.VehicleNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Handler global de exceções para toda a aplicação.
 *
 * <p>Mapeamentos de exceção → HTTP status:
 * <ul>
 *   <li>{@link ClientNotFoundException} / {@link VehicleNotFoundException} → 404 Not Found</li>
 *   <li>{@link IllegalArgumentException} → 400 Bad Request (entrada inválida, ex: CPF/placa inválidos)</li>
 *   <li>{@link IllegalStateException} → 409 Conflict (ex: CPF ou placa duplicados)</li>
 *   <li>{@link MethodArgumentNotValidException} → 400 Bad Request (Bean Validation)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata recursos não encontrados (cliente ou veículo).
     * Retorna HTTP 404 com a mensagem descritiva da exceção.
     */
    @ExceptionHandler({ClientNotFoundException.class, VehicleNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 404,
                "error", ex.getMessage()
        ));
    }

    /**
     * Trata erros de validação de dados de entrada (ex: CPF inválido, placa inválida).
     * Retorna HTTP 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400,
                "error", ex.getMessage()
        ));
    }

    /**
     * Trata conflitos de estado (ex: CPF ou placa já cadastrados).
     * Retorna HTTP 409.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 409,
                "error", ex.getMessage()
        ));
    }

    /**
     * Trata falhas do Bean Validation ({@code @NotBlank}, {@code @NotNull}, etc.).
     * Retorna HTTP 400 com a lista de campos inválidos e suas mensagens.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400,
                "errors", errors
        ));
    }
}
