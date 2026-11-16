package com.os.workshop.application.serviceorder;

import com.os.workshop.application.client.FindClientByCpfUseCase;
import com.os.workshop.application.notification.OrderStatusNotificationService;
import com.os.workshop.application.saga.OrderSagaEvent;
import com.os.workshop.application.service.CreateServiceUseCase;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.application.vehicle.FindVehicleByPlateUseCase;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import com.os.workshop.infrastructure.monitoring.ServiceOrderMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private final CreateServiceUseCase createServiceUseCase;
    private final FindClientByCpfUseCase findClientByCpfUseCase;
    private final FindVehicleByPlateUseCase findVehicleByPlateUseCase;
    private final ServiceOrderRepository serviceOrderRepository;
    private final OrderStatusNotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final ServiceOrderMetrics metrics;

    /**
     * Cria a OS e os servicos dela numa unica transacao.
     *
     * <p>Antes os servicos eram gravados um a um e a OS por ultimo, sem transacao:
     * se o save da OS falhasse, os servicos ficavam orfaos no banco. Agora tudo
     * entra ou nada entra, e o CRIAR_OS (enviado pelo
     * {@code OrderSagaEventListener} em AFTER_COMMIT) so sai depois que a OS
     * existe de fato.
     */
    @Transactional
    public ServiceOrder execute(String cpfCnpj, String placaVeiculo, List<String> serviceTypes) {
        try {
            findClientByCpfUseCase.execute(cpfCnpj);
            findVehicleByPlateUseCase.execute(placaVeiculo);

            ServiceOrder order = ServiceOrder.create(cpfCnpj, placaVeiculo, serviceTypes);

            serviceTypes.forEach(service -> createServiceUseCase.execute(service, order.getId()));

            ServiceOrder saved = serviceOrderRepository.save(order);

            notificationService.notifyStatusChange(saved);
            metrics.created();

            // Inicia a saga no os-orchestrator (CRIAR_OS). O envio ao SQS acontece
            // no OrderSagaEventListener, nao aqui.
            eventPublisher.publishEvent(
                    OrderSagaEvent.created(saved.getId(), saved.getStatus().name()));

            return saved;
        } catch (RuntimeException exception) {
            metrics.processingFailed("create");
            throw exception;
        }
    }
}
