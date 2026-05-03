package com.os.workshop.features.client.update;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.exception.ClientNotFoundException;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Atualiza os dados de contato de um cliente existente.
 * O CPF não pode ser alterado (é o identificador de negócio).
 */
@Service
@RequiredArgsConstructor
public class UpdateClientHandler {

    private final ClientRepository clientRepository;

    @Transactional
    public Client handle(Long id, UpdateClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("id: " + id));
        client.update(request.name(), request.email(), request.phone());
        return clientRepository.save(client);
    }
}
