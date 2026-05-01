package com.os.workshop.serviceorder.usecases;

import com.os.workshop.client.ClientService;
import com.os.workshop.service.domain.requests.CreateServiceRequest;
import com.os.workshop.service.usecases.CreateServiceUC;
import com.os.workshop.serviceorder.adapter.database.OrderRepository;
import com.os.workshop.serviceorder.domain.CreateOrderRequest;
import com.os.workshop.serviceorder.domain.OrderServiceStatusEnum;
import com.os.workshop.serviceorder.domain.ServiceOrderEntity;
import com.os.workshop.vehicle.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateOrderUC {

    private static final Logger logger = LoggerFactory.getLogger(CreateOrderUC.class);

    @Autowired
    private CreateServiceUC createServiceUC;

    @Autowired
    private ClientService clientService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private OrderRepository orderRepository;

    public ServiceOrderEntity process(CreateOrderRequest request) {

        var IdOrdemServico = UUID.randomUUID();

        clientService.findByCpf(request.getCpfCnpj());

        vehicleService.findByPlate(request.getPlacaVeiculo());

        request.getServiceTypes().forEach(
                service -> {
                    var serviceRequest = new CreateServiceRequest();
                    serviceRequest.setIdOS(IdOrdemServico);
                    serviceRequest.setServiceType(service);
                    createServiceUC.process(serviceRequest);
                }
        );

        ServiceOrderEntity order = new ServiceOrderEntity();
        order.setId(IdOrdemServico);
        order.setServiceTypeName(request.getServiceTypes().toString());
        order.setServiceStatus(OrderServiceStatusEnum.RECEBIDA.getStatus());
        order.setListService(request.getServiceTypes());
        order.setCpfCnpj(request.getCpfCnpj());
        order.setPlacaVeiculo(request.getPlacaVeiculo());

        orderRepository.save(order);

        logger.info("Ordem de serviço Criado.");



        logger.info("Mecanico deve ser notificado!");

        return order;
    }
}
