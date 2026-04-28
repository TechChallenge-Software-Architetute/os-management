package com.os.workshop.serviceorder.usecases;

import com.os.workshop.service.domain.requests.CreateServiceRequest;
import com.os.workshop.service.usecases.CreateServiceUC;
import com.os.workshop.serviceorder.adapter.api.OrderRepository;
import com.os.workshop.serviceorder.domain.CreateOrderRequest;
import com.os.workshop.serviceorder.domain.OrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateOrderUC {

    @Autowired
    private CreateServiceUC createServiceUC;

    @Autowired
    private OrderRepository orderRepository;

    public void process(CreateOrderRequest request) {

        var IdOrdemServico = UUID.randomUUID();

        //TODO: Identifica Cliente

        //TODO: Identifica Veiculo

        var listServicos = request.getServiceTypes().stream()
                .map(serviceType -> CreateServiceRequest.builder()
                        .ServiceType(serviceType)
                        .idOS(IdOrdemServico)
                        .build())
                .toList();

        listServicos.forEach(
                servico -> createServiceUC.process(servico)
        );

        //TODO: Salva Ordem de Serviço
        orderRepository.save(
                OrderEntity.builder()
                        .id(IdOrdemServico)
                        .serviceTypeName(request.getServiceTypes().toString())
                        .listService(listServicos.toString())
                        .build()
        );

        //TODO: Notifica Mecanico

    }
}
