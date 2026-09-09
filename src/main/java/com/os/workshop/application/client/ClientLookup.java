package com.os.workshop.application.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;

/**
 * Resolves the authenticated client from the identifier carried in the JWT subject.
 *
 * <p>The serverless CPF authentication function puts the client's CPF/CNPJ in the subject;
 * the legacy e-mail/password login puts the e-mail. Both are accepted so the client-portal
 * use cases work regardless of how the caller authenticated.
 */
final class ClientLookup {

    private ClientLookup() {
    }

    static Client resolve(ClientRepository clients, String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new ClientNotFoundException("identifier: <empty>");
        }
        String digits = identifier.replaceAll("\\D", "");
        if (digits.length() == 11 || digits.length() == 14) {
            var byDocument = clients.findByDocument(digits);
            if (byDocument.isPresent()) {
                return byDocument.get();
            }
        }
        return clients.findByEmail(identifier)
                .orElseThrow(() -> new ClientNotFoundException("identifier: " + identifier));
    }
}
