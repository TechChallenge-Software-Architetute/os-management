package com.os.workshop.application.serviceorder;

import com.os.workshop.application.client.FindClientByCpfUseCase;
import com.os.workshop.application.service.CreateServiceUseCase;
import com.os.workshop.application.serviceorder.port.out.ServiceOrderRepository;
import com.os.workshop.application.vehicle.FindVehicleByPlateUseCase;
import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private final CreateServiceUseCase createServiceUseCase;
    private final FindClientByCpfUseCase findClientByCpfUseCase;
    private final FindVehicleByPlateUseCase findVehicleByPlateUseCase;
    private final ServiceOrderRepository serviceOrderRepository;

    public ServiceOrder execute(String cpfCnpj, String placaVeiculo, List<String> serviceTypes) {
        var idOrdemServico = UUID.randomUUID();

        findClientByCpfUseCase.execute(cpfCnpj);
        findVehicleByPlateUseCase.execute(placaVeiculo);

        serviceTypes.forEach(service -> createServiceUseCase.execute(service, idOrdemServico));

        ServiceOrder order = ServiceOrder.builder()
                .id(idOrdemServico)
                .serviceTypeName(serviceTypes.toString())
                .serviceStatus(OrderServiceStatusEnum.RECEBIDA.getStatus())
                .listService(serviceTypes)
                .cpfCnpj(cpfCnpj)
                .placaVeiculo(placaVeiculo)
                .build();

        return serviceOrderRepository.save(order);
    }
}
