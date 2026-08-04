package com.os.workshop.application.notification;

import com.os.workshop.application.client.port.out.ClientRepository;
import com.os.workshop.application.notification.port.out.EmailNotificationPort;
import com.os.workshop.application.vehicle.port.out.VehicleRepository;
import com.os.workshop.domain.client.Client;
import com.os.workshop.domain.serviceorder.OrderStatusLabel;
import com.os.workshop.domain.serviceorder.ServiceOrder;
import com.os.workshop.domain.vehicle.Vehicle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusNotificationService {

    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final EmailNotificationPort emailNotificationPort;

    public void notifyStatusChange(ServiceOrder order) {
        try {
            Client client = clientRepository.findByDocument(order.getCpfCnpj()).orElse(null);
            if (client == null || client.getEmail() == null || client.getEmail().isBlank()) {
                log.warn("Cannot send notification for OS {}: client not found or no email", order.getId());
                return;
            }

            String vehicleInfo = buildVehicleInfo(order.getPlacaVeiculo());
            String statusLabel = OrderStatusLabel.of(order.getServiceStatus());

            emailNotificationPort.sendStatusUpdate(
                    client.getEmail(),
                    client.getName(),
                    vehicleInfo,
                    statusLabel
            );
        } catch (Exception e) {
            log.error("Failed to send status notification for OS {}: {}", order.getId(), e.getMessage(), e);
        }
    }

    private String buildVehicleInfo(String plate) {
        try {
            Vehicle vehicle = vehicleRepository.findByPlate(plate).orElse(null);
            if (vehicle != null) {
                return vehicle.getBrand() + " " + vehicle.getModel() + " (" + plate + ")";
            }
        } catch (Exception e) {
            log.warn("Could not fetch vehicle info for plate {}: {}", plate, e.getMessage());
        }
        return plate;
    }
}
