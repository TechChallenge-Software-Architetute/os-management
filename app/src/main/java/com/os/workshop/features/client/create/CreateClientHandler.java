package com.os.workshop.features.client.create;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastra um novo cliente após validar unicidade do CPF.
 */
@Service
@RequiredArgsConstructor
public class CreateClientHandler {

    private final ClientRepository clientRepository;

    @Transactional
    public Client handle(CreateClientRequest request) {
        String normalizedCpf = normalizeCpf(request.cpf());
        if (clientRepository.existsByCpf(normalizedCpf)) {
            throw new IllegalStateException("A client with CPF '" + request.cpf() + "' already exists");
        }
        Client client = Client.create(request.name(), request.cpf(), request.email(), request.phone());
        return clientRepository.save(client);
    }

    private static String normalizeCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("[^0-9]", "");
    }
}
