package com.os.workshop.features.serviceorder.create;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderResponse;
import com.os.workshop.application.client.FindClientByCpfUseCase;
import com.os.workshop.application.service.CreateServiceUseCase;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;
import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import com.os.workshop.features.serviceorder.shared.mapper.ServiceOrderMapper;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import com.os.workshop.application.vehicle.FindVehicleByPlateUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateOrderHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateOrderHandler.class);

    @Autowired
    private CreateServiceUseCase createServiceUseCase;

    @Autowired
    private FindClientByCpfUseCase findClientByCpfUseCase;

    @Autowired
    private FindVehicleByPlateUseCase findVehicleByPlateUseCase;

    @Autowired
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Autowired
    private FindBudgetByServiceOrderHandler findBudgetByServiceOrderHandler;

    public ServiceOrder handle(CreateOrderRequest request) {

        var idOrdemServico = UUID.randomUUID();

        findClientByCpfUseCase.execute(request.getCpfCnpj());

        findVehicleByPlateUseCase.execute(request.getPlacaVeiculo());

        request.getServiceTypes().forEach(
                service -> {
                    createServiceUseCase.execute(service, idOrdemServico);
                }
        );

        ServiceOrderEntity order = new ServiceOrderEntity();
        order.setId(idOrdemServico);
        order.setServiceTypeName(request.getServiceTypes().toString());
        order.setServiceStatus(OrderServiceStatusEnum.RECEBIDA.getStatus());
        order.setListService(request.getServiceTypes());
        order.setCpfCnpj(request.getCpfCnpj());
        order.setPlacaVeiculo(request.getPlacaVeiculo());

        serviceOrderJpaRepository.save(order);

        logger.info("Ordem de serviço Criado.");

        logger.info("Mecanico deve ser notificado!");

        var budget = findBudgetByServiceOrderHandler.handle(order.getId())
                .map(FindBudgetByServiceOrderResponse::from)
                .orElse(null);

        return ServiceOrderMapper.toDomain(order, budget);
    }
}
