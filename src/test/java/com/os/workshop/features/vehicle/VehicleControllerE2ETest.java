package com.os.workshop.features.vehicle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.os.workshop.adapter.in.web.vehicle.VehicleController;
import com.os.workshop.application.vehicle.CreateVehicleUseCase;
import com.os.workshop.adapter.in.web.vehicle.CreateVehicleRequest;
import com.os.workshop.application.vehicle.DeactivateVehicleUseCase;
import com.os.workshop.application.vehicle.FindVehiclesByClientUseCase;
import com.os.workshop.application.vehicle.FindVehicleByIdUseCase;
import com.os.workshop.application.vehicle.FindVehicleByPlateUseCase;
import com.os.workshop.domain.vehicle.Vehicle;
import com.os.workshop.domain.vehicle.VehicleType;
import com.os.workshop.application.vehicle.UpdateVehicleUseCase;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VehicleControllerE2ETest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private CreateVehicleUseCase createVehicleUseCase;
    @Mock private FindVehicleByIdUseCase findVehicleByIdUseCase;
    @Mock private FindVehicleByPlateUseCase findVehicleByPlateUseCase;
    @Mock private FindVehiclesByClientUseCase findVehiclesByClientUseCase;
    @Mock private UpdateVehicleUseCase updateVehicleUseCase;
    @Mock private DeactivateVehicleUseCase deactivateVehicleUseCase;

    @BeforeEach
    void setUp() {
        VehicleController controller = new VehicleController(
                createVehicleUseCase, findVehicleByIdUseCase, findVehicleByPlateUseCase,
                findVehiclesByClientUseCase, updateVehicleUseCase, deactivateVehicleUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Vehicle createVehicle() {
        return Vehicle.reconstitute(1L, 1L, "ABC1234", "TOYOTA", "COROLLA",
                2020, "WHITE", VehicleType.CAR, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void whenCreatingVehicleWithValidData_thenReturns201() throws Exception {
        when(createVehicleUseCase.execute(anyLong(), anyString(), anyString(),
                anyString(), anyInt(), anyString(), any(VehicleType.class)))
                .thenReturn(createVehicle());

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
        when(findVehicleByIdUseCase.execute(1L)).thenReturn(createVehicle());

        mockMvc.perform(get("/api/vehicles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC-1234"));
    }

    @Test
    void whenFindingVehicleByPlate_thenReturns200() throws Exception {
        when(findVehicleByPlateUseCase.execute("ABC1234")).thenReturn(createVehicle());

        mockMvc.perform(get("/api/vehicles/plate/{plate}", "ABC1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC-1234"));
    }

    @Test
    void whenListingVehiclesByClient_thenReturns200() throws Exception {
        when(findVehiclesByClientUseCase.execute(1L)).thenReturn(List.of(createVehicle()));

        mockMvc.perform(get("/api/vehicles/client/{clientId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].plate").value("ABC-1234"));
    }

    @Test
    void whenDeactivatingVehicle_thenReturns204() throws Exception {
        doNothing().when(deactivateVehicleUseCase).execute(1L);

        mockMvc.perform(delete("/api/vehicles/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
