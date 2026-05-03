package com.os.workshop.features.vehicle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.os.workshop.features.client.domain.Client;
import com.os.workshop.features.client.repository.ClientRepository;
import com.os.workshop.features.vehicle.domain.Vehicle;
import com.os.workshop.features.vehicle.domain.VehicleType;
import com.os.workshop.features.vehicle.dto.VehicleRequest;
import com.os.workshop.features.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VehicleControllerE2ETest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ClientRepository clientRepository;

    @BeforeEach
    void setUp() {
        VehicleService vehicleService = new VehicleService(vehicleRepository, clientRepository);
        VehicleController vehicleController = new VehicleController(vehicleService);
        mockMvc = MockMvcBuilders.standaloneSetup(vehicleController).build();
    }

    private Vehicle createVehicle() {
        return Vehicle.reconstitute(1L, 1L, "ABC1234", "TOYOTA", "COROLLA",
                2020, "WHITE", VehicleType.CAR, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Client createClient() {
        return Client.reconstitute(1L, "JOHN DOE", "52998224725",
                "john@email.com", "11999999999", true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void whenCreatingVehicleWithValidData_thenReturns201() throws Exception {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(createClient()));
        when(vehicleRepository.existsByPlate("ABC1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(createVehicle());

        VehicleRequest request = new VehicleRequest(1L, "ABC1234", "Toyota", "Corolla",
                2020, "White", VehicleType.CAR);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plate").value("ABC-1234"))
                .andExpect(jsonPath("$.brand").value("TOYOTA"));
    }

    @Test
    void whenFindingVehicleByExistingId_thenReturns200() throws Exception {
        Vehicle vehicle = createVehicle();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        mockMvc.perform(get("/api/vehicles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC-1234"));
    }

    @Test
    void whenFindingVehicleByPlate_thenReturns200() throws Exception {
        Vehicle vehicle = createVehicle();
        when(vehicleRepository.findByPlate("ABC1234")).thenReturn(Optional.of(vehicle));

        mockMvc.perform(get("/api/vehicles/plate/{plate}", "ABC1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC-1234"));
    }

    @Test
    void whenListingVehiclesByClient_thenReturns200() throws Exception {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(createClient()));
        when(vehicleRepository.findAllByClientId(1L)).thenReturn(List.of(createVehicle()));

        mockMvc.perform(get("/api/vehicles/client/{clientId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].plate").value("ABC-1234"));
    }

    @Test
    void whenDeactivatingVehicle_thenReturns204() throws Exception {
        Vehicle vehicle = createVehicle();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        mockMvc.perform(delete("/api/vehicles/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
