package com.os.workshop.serviceorder.domain;

import com.os.workshop.features.serviceorder.domain.CreateOrderRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateOrderRequestTest {

    @Test
    void storesRequestValues() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCpfCnpj("52998224725");
        request.setPlacaVeiculo("ABC1234");
        request.setServiceTypes(List.of("REVISAO"));

        assertEquals("52998224725", request.getCpfCnpj());
        assertEquals("ABC1234", request.getPlacaVeiculo());
        assertEquals(List.of("REVISAO"), request.getServiceTypes());
    }
}
