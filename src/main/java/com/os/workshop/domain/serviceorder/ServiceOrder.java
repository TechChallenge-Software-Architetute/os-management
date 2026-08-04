package com.os.workshop.domain.serviceorder;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ServiceOrder {

    private UUID id;
    private String serviceTypeName;
    private OrderServiceStatusEnum status;
    private List<String> listService;
    private String cpfCnpj;
    private String placaVeiculo;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected ServiceOrder() {}

    public static ServiceOrder create(String cpfCnpj, String placaVeiculo, List<String> serviceTypes) {
        Objects.requireNonNull(cpfCnpj, "CPF/CNPJ is required");
        Objects.requireNonNull(placaVeiculo, "Placa do veiculo is required");
        if (serviceTypes == null || serviceTypes.isEmpty()) {
            throw new IllegalArgumentException("At least one service type is required");
        }

        var order = new ServiceOrder();
        order.id = UUID.randomUUID();
        order.cpfCnpj = cpfCnpj.strip();
        order.placaVeiculo = placaVeiculo.strip().toUpperCase();
        order.listService = List.copyOf(serviceTypes);
        order.serviceTypeName = serviceTypes.toString();
        order.status = OrderServiceStatusEnum.RECEBIDA;
        return order;
    }

    public static ServiceOrder reconstitute(UUID id, String cpfCnpj, String placaVeiculo,
                                             List<String> serviceTypes, String serviceTypeName,
                                             OrderServiceStatusEnum status, String rejectionReason,
                                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        var order = new ServiceOrder();
        order.id = id;
        order.cpfCnpj = cpfCnpj;
        order.placaVeiculo = placaVeiculo;
        order.listService = serviceTypes;
        order.serviceTypeName = serviceTypeName;
        order.status = status;
        order.rejectionReason = rejectionReason;
        order.createdAt = createdAt;
        order.updatedAt = updatedAt;
        return order;
    }

    public void advanceTo(OrderServiceStatusEnum newStatus) {
        Objects.requireNonNull(newStatus, "Target status is required");
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition from " + this.status.getStatus()
                            + " to " + newStatus.getStatus()
                            + ". Allowed transitions: " + this.status.getAllowedTransitions());
        }
        this.status = newStatus;
    }

    public void reject(String reason) {
        advanceTo(OrderServiceStatusEnum.RECUSADA);
        this.rejectionReason = reason;
    }

    public void reopen() {
        advanceTo(OrderServiceStatusEnum.EM_DIAGNOSTICO);
    }

    public String getServiceStatus() {
        return status.getStatus();
    }
}
