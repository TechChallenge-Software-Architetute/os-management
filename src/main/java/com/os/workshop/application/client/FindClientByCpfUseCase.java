package com.os.workshop.application.client;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.client.ClientNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindClientByCpfUseCase {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public Client execute(String document) {
        String normalized = document == null ? "" : document.replaceAll("[^0-9]", "");
        return clientRepository.findByDocument(normalized)
                .orElseThrow(() -> new ClientNotFoundException("Document: " + document));
    }
}
