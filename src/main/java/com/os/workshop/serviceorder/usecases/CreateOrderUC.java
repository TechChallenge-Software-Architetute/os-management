package com.os.workshop.serviceorder.usecases;

import com.os.workshop.service.domain.enums.ServiceStatusEnum;
import com.os.workshop.service.domain.requests.CreateServiceRequest;
import com.os.workshop.service.usecases.CreateServiceUC;
import com.os.workshop.serviceorder.adapter.api.OrderRepository;
import com.os.workshop.serviceorder.domain.CreateOrderRequest;
import com.os.workshop.serviceorder.domain.ServiceOrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CreateOrderUC {

    @Autowired
    private CreateServiceUC createServiceUC;

    @Autowired
    private OrderRepository orderRepository;

    public ServiceOrderEntity process(CreateOrderRequest request) {

        var IdOrdemServico = UUID.randomUUID();

        //TODO: Identifica Cliente

        //TODO: Identifica Veiculo


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
        order.setServiceStatus(ServiceStatusEnum.TO_DO.getStatus());
        order.setListService(request.getServiceTypes());

        //TODO: Salva Ordem de Serviço
        orderRepository.save(order);

        //TODO: Notifica Mecanico


        return order;
    }
}
