package com.os.workshop.application.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateClientUseCase {

    private final ClientRepository clientRepository;

    @Transactional
    public Client execute(Long id, String name, String email, String phone) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("id: " + id));
        client.update(name, email, phone);
        return clientRepository.save(client);
    }
}
