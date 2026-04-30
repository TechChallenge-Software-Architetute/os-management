package com.os.features.client.exception;

/**
 * Lançada quando um cliente não é encontrado pelo identificador informado.
 * Mapeada para HTTP 404 pelo {@code GlobalExceptionHandler}.
 */
public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(String identifier) {
        super("Client not found: " + identifier);
    }
}
