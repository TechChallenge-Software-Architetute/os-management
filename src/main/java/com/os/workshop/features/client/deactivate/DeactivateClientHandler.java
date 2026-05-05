package com.os.workshop.features.client.deactivate;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.exception.ClientNotFoundException;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Desativa um cliente (soft delete).
 * O registro permanece no banco mas é excluído das listagens ativas.
 */
@Service
@RequiredArgsConstructor
public class DeactivateClientHandler {

    private final ClientRepository clientRepository;

    @Transactional
    public void handle(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("id: " + id));
        client.deactivate();
        clientRepository.save(client);
    }
}
