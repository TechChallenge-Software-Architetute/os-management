package com.os.workshop.features.client;

import com.os.workshop.features.client.domain.Client;
import com.os.workshop.features.client.dto.ClientRequest;
import com.os.workshop.features.client.exception.ClientNotFoundException;
import com.os.workshop.features.client.repository.ClientRepository;
import com.os.workshop.features.budget.BudgetResponse;
import com.os.workshop.features.budget.BudgetService;
import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
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
    private final ServiceOrderJpaRepository serviceOrderJpaRepository;
    private final BudgetService budgetService;

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
    public Client findById(Long id) {
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
    public Client update(Long id, ClientRequest request) {
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
    public void deactivate(Long id) {
        Client client = findById(id);
        client.deactivate();
        clientRepository.save(client);
    }

    /**
     * Finds a client by their email address.
     * Used to resolve the logged-in user's client profile from the JWT token.
     *
     * @param email the client's email
     * @return the client
     * @throws ClientNotFoundException if no client exists with the given email
     */
    @Transactional(readOnly = true)
    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("email: " + email));
    }

    /**
     * Returns all service orders for the logged-in client.
     * Resolves the client from their email and looks up orders by CPF.
     *
     * @param email the authenticated user's email (from JWT)
     * @return list of service order entities for the client
     * @throws ClientNotFoundException if no client exists with the given email
     */
    @Transactional(readOnly = true)
    public List<ServiceOrderEntity> findMyOrders(String email) {
        Client client = findByEmail(email);
        return serviceOrderJpaRepository.findByCpfCnpj(client.getCpf().getValue());
    }

    /**
     * Returns a single service order by ID, validating it belongs to the logged-in client.
     *
     * @param email   the authenticated user's email (from JWT)
     * @param orderId the service order UUID
     * @return the service order entity
     * @throws ClientNotFoundException  if the client doesn't exist
     * @throws IllegalArgumentException if the order doesn't exist or doesn't belong to this client
     */
    @Transactional(readOnly = true)
    public ServiceOrderEntity findMyOrderById(String email, UUID orderId) {
        Client client = findByEmail(email);

        ServiceOrderEntity order = serviceOrderJpaRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (!order.getCpfCnpj().equals(client.getCpf().getValue())) {
            throw new IllegalArgumentException("Order " + orderId + " does not belong to this client");
        }

        return order;
    }

    /**
     * Returns the budget for a service order, if available.
     *
     * @param serviceOrderId the service order UUID
     * @return the budget response, or null if no budget exists
     */
    @Transactional(readOnly = true)
    public BudgetResponse findBudgetForOrder(UUID serviceOrderId) {
        return budgetService.findByServiceOrderId(serviceOrderId)
                .map(BudgetResponse::from)
                .orElse(null);
    }

    /**
     * Approves a service order on behalf of the logged-in client.
     * The order must be in AGUARDANDO_APROVACAO status and belong to the client.
     *
     * @param email   the authenticated user's email (from JWT)
     * @param orderId the service order UUID to approve
     * @return the updated service order entity
     * @throws ClientNotFoundException  if the client doesn't exist
     * @throws IllegalArgumentException if the order doesn't exist or doesn't belong to the client
     * @throws IllegalStateException    if the order is not in AGUARDANDO_APROVACAO status
     */
    @Transactional
    public ServiceOrderEntity approveMyOrder(String email, UUID orderId) {
        ServiceOrderEntity order = findMyOrderById(email, orderId);

        if (!OrderServiceStatusEnum.AGUARDANDO_APROVACAO.getStatus().equals(order.getServiceStatus())) {
            throw new IllegalStateException(
                    "Order " + orderId + " cannot be approved. Current status: " + order.getServiceStatus()
                            + ". Expected: " + OrderServiceStatusEnum.AGUARDANDO_APROVACAO.getStatus());
        }

        order.setServiceStatus(OrderServiceStatusEnum.APROVADO.getStatus());
        return serviceOrderJpaRepository.save(order);
    }

    /** Remove pontuação do CPF para consultas e validações de unicidade. */
    private static String normalizeCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("[^0-9]", "");
    }
}
