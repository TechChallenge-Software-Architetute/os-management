package com.os.workshop.features.client.findByCpf;

import com.os.workshop.features.client.shared.domain.Client;
import com.os.workshop.features.client.shared.exception.ClientNotFoundException;
import com.os.workshop.features.client.shared.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Busca um cliente pelo CPF.
 * Normaliza o CPF antes da consulta (aceita entrada com ou sem formatação).
 */
@Service
@RequiredArgsConstructor
public class FindClientByCpfHandler {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public Client handle(String cpf) {
        return clientRepository.findByCpf(normalizeCpf(cpf))
                .orElseThrow(() -> new ClientNotFoundException("CPF: " + cpf));
    }

    private static String normalizeCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("[^0-9]", "");
    }
}
