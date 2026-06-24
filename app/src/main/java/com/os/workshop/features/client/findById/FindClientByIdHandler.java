package com.os.workshop.features.client.findById;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.exception.ClientNotFoundException;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Busca um cliente pelo seu ID interno.
 */
@Service
@RequiredArgsConstructor
public class FindClientByIdHandler {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public Client handle(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("id: " + id));
    }
}
