package com.os.workshop.domain.client;

/**
 * Lançada quando um cliente não é encontrado pelo identificador informado.
 */
public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(String identifier) {
        super("Client not found: " + identifier);
    }
}
