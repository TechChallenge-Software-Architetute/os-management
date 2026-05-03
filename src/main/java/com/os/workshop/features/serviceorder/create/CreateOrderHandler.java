package com.os.workshop.features.serviceorder.create;

import com.os.workshop.features.budget.BudgetResponse;
import com.os.workshop.features.budget.BudgetService;
import com.os.workshop.features.client.ClientService;
import com.os.workshop.features.service.domain.requests.CreateServiceRequest;
import com.os.workshop.features.service.usecases.CreateServiceUC;
import com.os.workshop.features.serviceorder.shared.domain.ServiceOrder;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderEntity;
import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import com.os.workshop.features.serviceorder.shared.repository.ServiceOrderJpaRepository;
import com.os.workshop.features.serviceorder.shared.mapper.ServiceOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.os.workshop.features.vehicle.VehicleService;

import java.util.UUID;

@Service
public class CreateOrderHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateOrderHandler.class);

    @Autowired
    private CreateServiceUC createServiceUC;

    @Autowired
    private ClientService clientService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ServiceOrderJpaRepository serviceOrderJpaRepository;

    @Autowired
    private BudgetService budgetService;

    public ServiceOrder handle(CreateOrderRequest request) {

        var idOrdemServico = UUID.randomUUID();

        clientService.findByCpf(request.getCpfCnpj());

        vehicleService.findByPlate(request.getPlacaVeiculo());

        request.getServiceTypes().forEach(
                service -> {
                    var serviceRequest = new CreateServiceRequest();
                    serviceRequest.setIdOS(idOrdemServico);
                    serviceRequest.setServiceType(service);
                    createServiceUC.process(serviceRequest);
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

        var budget = budgetService.findByServiceOrderId(order.getId())
                .map(BudgetResponse::from)
                .orElse(null);

        return ServiceOrderMapper.toDomain(order, budget);
    }
}
