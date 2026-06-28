package com.os.workshop.features.persistence.entity;

import com.os.workshop.features.budget.shared.repository.BudgetEntity;
import com.os.workshop.infrastructure.persistence.client.ClientEntity;
import com.os.workshop.features.stock.shared.repository.StockEntity;
import com.os.workshop.features.stock.shared.repository.StockMovementEntity;
import com.os.workshop.features.stock.shared.repository.StockReservationEntity;
import com.os.workshop.infrastructure.persistence.vehicle.VehicleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JpaEntityLifecycleTest {

    @Test
    void lifecycleMethodsSetTimestamps() {
        BudgetEntity budget = new BudgetEntity();
        ClientEntity client = new ClientEntity();
        StockEntity stock = new StockEntity();
        StockMovementEntity movement = new StockMovementEntity();
        StockReservationEntity reservation = new StockReservationEntity();
        VehicleEntity vehicle = new VehicleEntity();

        ReflectionTestUtils.invokeMethod(budget, "onCreate");
        ReflectionTestUtils.invokeMethod(client, "onCreate");
        ReflectionTestUtils.invokeMethod(stock, "onCreate");
        ReflectionTestUtils.invokeMethod(movement, "onCreate");
        ReflectionTestUtils.invokeMethod(reservation, "onCreate");
        ReflectionTestUtils.invokeMethod(vehicle, "onCreate");

        assertNotNull(budget.getCreatedAt());
        assertNotNull(client.getUpdatedAt());
        assertNotNull(stock.getCreatedAt());
        assertNotNull(movement.getCreatedAt());
        assertNotNull(reservation.getUpdatedAt());
        assertNotNull(vehicle.getCreatedAt());

        ReflectionTestUtils.invokeMethod(budget, "onUpdate");
        ReflectionTestUtils.invokeMethod(client, "onUpdate");
        ReflectionTestUtils.invokeMethod(stock, "onUpdate");
        ReflectionTestUtils.invokeMethod(reservation, "onUpdate");
        ReflectionTestUtils.invokeMethod(vehicle, "onUpdate");

        assertNotNull(budget.getUpdatedAt());
        assertNotNull(client.getUpdatedAt());
        assertNotNull(stock.getUpdatedAt());
        assertNotNull(reservation.getUpdatedAt());
        assertNotNull(vehicle.getUpdatedAt());
    }
}
