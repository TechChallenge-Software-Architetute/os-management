package com.os.workshop.application.notification;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.notification.port.out.EmailNotificationPort;
import com.os.workshop.application.vehicle.port.out.VehicleRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import com.os.workshop.domain.vehicle.Vehicle;
import com.os.workshop.domain.vehicle.VehicleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderStatusNotificationServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private EmailNotificationPort emailNotificationPort;
    @InjectMocks private OrderStatusNotificationService service;

    private ServiceOrder createOrder() {
        return ServiceOrder.reconstitute(UUID.randomUUID(), "52998224725", "ABC1D23",
                List.of("TROCA_OLEO"), "[TROCA_OLEO]",
                OrderServiceStatusEnum.EM_DIAGNOSTICO, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void sendsEmailWhenClientAndVehicleFound() {
        ServiceOrder order = createOrder();
        Client client = Client.reconstitute(1L, "JOAO", "52998224725", "joao@email.com",
                "11999999999", true, LocalDateTime.now(), LocalDateTime.now());
        Vehicle vehicle = Vehicle.reconstitute(1L, 1L, "ABC1D23", "TOYOTA", "COROLLA",
                2020, "PRATA", VehicleType.CAR, true, LocalDateTime.now(), LocalDateTime.now());

        when(clientRepository.findByDocument("52998224725")).thenReturn(Optional.of(client));
        when(vehicleRepository.findByPlate("ABC1D23")).thenReturn(Optional.of(vehicle));

        service.notifyStatusChange(order);

        verify(emailNotificationPort).sendStatusUpdate(
                eq("joao@email.com"),
                eq("JOAO"),
                eq("TOYOTA COROLLA (ABC1D23)"),
                eq("Em Diagnostico")
        );
    }

    @Test
    void doesNotSendWhenClientNotFound() {
        ServiceOrder order = createOrder();
        when(clientRepository.findByDocument("52998224725")).thenReturn(Optional.empty());

        service.notifyStatusChange(order);

        verify(emailNotificationPort, never()).sendStatusUpdate(any(), any(), any(), any());
    }

    @Test
    void doesNotSendWhenClientHasNoEmail() {
        ServiceOrder order = createOrder();
        Client client = Client.reconstitute(1L, "JOAO", "52998224725", null,
                "11999999999", true, LocalDateTime.now(), LocalDateTime.now());

        when(clientRepository.findByDocument("52998224725")).thenReturn(Optional.of(client));

        service.notifyStatusChange(order);

        verify(emailNotificationPort, never()).sendStatusUpdate(any(), any(), any(), any());
    }

    @Test
    void sendsWithPlateOnlyWhenVehicleNotFound() {
        ServiceOrder order = createOrder();
        Client client = Client.reconstitute(1L, "JOAO", "52998224725", "joao@email.com",
                "11999999999", true, LocalDateTime.now(), LocalDateTime.now());

        when(clientRepository.findByDocument("52998224725")).thenReturn(Optional.of(client));
        when(vehicleRepository.findByPlate("ABC1D23")).thenReturn(Optional.empty());

        service.notifyStatusChange(order);

        verify(emailNotificationPort).sendStatusUpdate(
                eq("joao@email.com"),
                eq("JOAO"),
                eq("ABC1D23"),
                eq("Em Diagnostico")
        );
    }

    @Test
    void doesNotThrowWhenVehicleRepoFails() {
        ServiceOrder order = createOrder();
        Client client = Client.reconstitute(1L, "JOAO", "52998224725", "joao@email.com",
                "11999999999", true, LocalDateTime.now(), LocalDateTime.now());

        when(clientRepository.findByDocument("52998224725")).thenReturn(Optional.of(client));
        when(vehicleRepository.findByPlate("ABC1D23")).thenThrow(new RuntimeException("DB error"));

        service.notifyStatusChange(order);

        verify(emailNotificationPort).sendStatusUpdate(
                eq("joao@email.com"),
                eq("JOAO"),
                eq("ABC1D23"),
                eq("Em Diagnostico")
        );
    }
}
