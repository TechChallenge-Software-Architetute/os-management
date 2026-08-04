package com.os.workshop.application.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.domain.client.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateClientUseCase {

    private final ClientRepository clientRepository;

    @Transactional
    public Client execute(String name, String rawDocument, String email, String phone) {
        String normalized = rawDocument == null ? "" : rawDocument.replaceAll("[^0-9]", "");
        if (clientRepository.existsByDocument(normalized)) {
            throw new IllegalStateException("A client with document '" + rawDocument + "' already exists");
        }
        Client client = Client.create(name, rawDocument, email, phone);
        return clientRepository.save(client);
    }
}
