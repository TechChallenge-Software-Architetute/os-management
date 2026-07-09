package com.os.workshop.application.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivateClientUseCase {

    private final ClientRepository clientRepository;

    @Transactional
    public void execute(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("id: " + id));
        client.deactivate();
        clientRepository.save(client);
    }
}
