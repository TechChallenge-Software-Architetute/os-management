package com.os.workshop.features.vehicle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.os.workshop.features.vehicle.create.CreateVehicleHandler;
import com.os.workshop.features.vehicle.create.CreateVehicleRequest;
import com.os.workshop.features.vehicle.deactivate.DeactivateVehicleHandler;
import com.os.workshop.features.vehicle.findByClient.FindVehiclesByClientHandler;
import com.os.workshop.features.vehicle.findById.FindVehicleByIdHandler;
import com.os.workshop.features.vehicle.findByPlate.FindVehicleByPlateHandler;
import com.os.workshop.features.vehicle.shared.domain.Vehicle;
import com.os.workshop.features.vehicle.shared.domain.VehicleType;
import com.os.workshop.features.vehicle.update.UpdateVehicleHandler;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VehicleControllerE2ETest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private CreateVehicleHandler createVehicleHandler;
    @Mock private FindVehicleByIdHandler findVehicleByIdHandler;
    @Mock private FindVehicleByPlateHandler findVehicleByPlateHandler;
    @Mock private FindVehiclesByClientHandler findVehiclesByClientHandler;
    @Mock private UpdateVehicleHandler updateVehicleHandler;
    @Mock private DeactivateVehicleHandler deactivateVehicleHandler;

    @BeforeEach
    void setUp() {
        VehicleController controller = new VehicleController(
                createVehicleHandler, findVehicleByIdHandler, findVehicleByPlateHandler,
                findVehiclesByClientHandler, updateVehicleHandler, deactivateVehicleHandler);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Vehicle createVehicle() {
        return Vehicle.reconstitute(1L, 1L, "ABC1234", "TOYOTA", "COROLLA",
                2020, "WHITE", VehicleType.CAR, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void whenCreatingVehicleWithValidData_thenReturns201() throws Exception {
        when(createVehicleHandler.handle(any(CreateVehicleRequest.class))).thenReturn(createVehicle());

        CreateVehicleRequest request = new CreateVehicleRequest(1L, "ABC1234", "Toyota", "Corolla",
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
        when(findVehicleByIdHandler.handle(1L)).thenReturn(createVehicle());

        mockMvc.perform(get("/api/vehicles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC-1234"));
    }

    @Test
    void whenFindingVehicleByPlate_thenReturns200() throws Exception {
        when(findVehicleByPlateHandler.handle("ABC1234")).thenReturn(createVehicle());

        mockMvc.perform(get("/api/vehicles/plate/{plate}", "ABC1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC-1234"));
    }

    @Test
    void whenListingVehiclesByClient_thenReturns200() throws Exception {
        when(findVehiclesByClientHandler.handle(1L)).thenReturn(List.of(createVehicle()));

        mockMvc.perform(get("/api/vehicles/client/{clientId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].plate").value("ABC-1234"));
    }

    @Test
    void whenDeactivatingVehicle_thenReturns204() throws Exception {
        doNothing().when(deactivateVehicleHandler).handle(1L);

        mockMvc.perform(delete("/api/vehicles/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
