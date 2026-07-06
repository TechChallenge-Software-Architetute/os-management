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
    public Client execute(String name, String cpf, String email, String phone) {
        String normalizedCpf = cpf == null ? "" : cpf.replaceAll("[^0-9]", "");
        if (clientRepository.existsByCpf(normalizedCpf)) {
            throw new IllegalStateException("A client with CPF '" + cpf + "' already exists");
        }
        Client client = Client.create(name, cpf, email, phone);
        return clientRepository.save(client);
    }
}
