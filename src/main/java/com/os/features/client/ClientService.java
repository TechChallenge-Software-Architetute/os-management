package com.os.features.client;

import com.os.features.client.domain.Client;
import com.os.features.client.exception.ClientNotFoundException;
import com.os.features.client.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço de aplicação responsável pelos casos de uso de cliente.
 *
 * <p>Orquestra as operações delegando ao {@link ClientRepository} (porta de saída) e
 * invocando as regras de negócio encapsuladas no aggregate {@link Client}.
 * Não contém lógica de domínio — apenas coordenação de fluxo e tratamento de erros de aplicação.
 */
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    /**
     * Cadastra um novo cliente após validar unicidade do CPF.
     *
     * @param request dados do cliente a cadastrar
     * @return cliente persistido com ID e timestamps preenchidos
     * @throws IllegalStateException    se o CPF já estiver cadastrado
     * @throws IllegalArgumentException se o CPF for inválido (lançado pelo value object)
     */
    @Transactional
    public Client create(ClientRequest request) {
        String normalizedCpf = normalizeCpf(request.cpf());
        if (clientRepository.existsByCpf(normalizedCpf)) {
            throw new IllegalStateException("A client with CPF '" + request.cpf() + "' already exists");
        }
        Client client = Client.create(request.name(), request.cpf(), request.email(), request.phone());
        return clientRepository.save(client);
    }

    /**
     * Busca um cliente pelo seu ID interno.
     *
     * @param id identificador UUID do cliente
     * @return cliente encontrado
     * @throws ClientNotFoundException se nenhum cliente existir com o ID informado
     */
    @Transactional(readOnly = true)
    public Client findById(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("id: " + id));
    }

    /**
     * Busca um cliente pelo CPF.
     * Normaliza o CPF antes da consulta (aceita entrada com ou sem formatação).
     *
     * @param cpf CPF com ou sem formatação
     * @return cliente encontrado
     * @throws ClientNotFoundException se nenhum cliente existir com o CPF informado
     */
    @Transactional(readOnly = true)
    public Client findByCpf(String cpf) {
        return clientRepository.findByCpf(normalizeCpf(cpf))
                .orElseThrow(() -> new ClientNotFoundException("CPF: " + cpf));
    }

    /**
     * Retorna todos os clientes ativos cadastrados no sistema.
     *
     * @return lista de clientes com {@code active = true}
     */
    @Transactional(readOnly = true)
    public List<Client> findAll() {
        return clientRepository.findAllActive();
    }

    /**
     * Atualiza os dados de contato de um cliente existente.
     * O CPF não pode ser alterado (é o identificador de negócio).
     *
     * @param id      ID do cliente a atualizar
     * @param request novos dados do cliente
     * @return cliente atualizado
     * @throws ClientNotFoundException se o cliente não for encontrado
     */
    @Transactional
    public Client update(UUID id, ClientRequest request) {
        Client client = findById(id);
        client.update(request.name(), request.email(), request.phone());
        return clientRepository.save(client);
    }

    /**
     * Desativa um cliente (soft delete).
     * O registro permanece no banco mas é excluído das listagens ativas.
     *
     * @param id ID do cliente a desativar
     * @throws ClientNotFoundException se o cliente não for encontrado
     */
    @Transactional
    public void deactivate(UUID id) {
        Client client = findById(id);
        client.deactivate();
        clientRepository.save(client);
    }

    /** Remove pontuação do CPF para consultas e validações de unicidade. */
    private static String normalizeCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("[^0-9]", "");
    }
}
