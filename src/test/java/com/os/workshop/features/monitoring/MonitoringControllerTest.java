package com.os.workshop.features.monitoring;

import com.os.workshop.features.monitoring.averageExecutionTime.GetAverageExecutionTimeHandler;
import com.os.workshop.features.monitoring.averageExecutionTime.GetAverageExecutionTimeRequest;
import com.os.workshop.features.monitoring.averageExecutionTimeById.GetAverageExecutionTimeByIdHandler;
import com.os.workshop.features.monitoring.shared.domain.AverageTimeEnum;
import com.os.workshop.features.monitoring.shared.domain.ServiceAverageTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MonitoringControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetAverageExecutionTimeHandler getAverageExecutionTimeHandler;

    @Mock
    private GetAverageExecutionTimeByIdHandler getAverageExecutionTimeByIdHandler;

    @InjectMocks
    private MonitoringController monitoringController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(monitoringController).build();
    }

    @Test
    void whenGettingAverageExecutionTimeWithValidRequest_thenReturns200() throws Exception {
        List<ServiceAverageTime> mockAverages = List.of(
                new ServiceAverageTime("ServiceType1", 10.5),
                new ServiceAverageTime("ServiceType2", 20.0)
        );
        when(getAverageExecutionTimeHandler.handle(any(GetAverageExecutionTimeRequest.class)))
                .thenReturn(mockAverages);

        String requestJson = "{\"timeUnit\":\"SECONDS\"}";

        mockMvc.perform(post("/monitoring/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].serviceTypeName").value("ServiceType1"))
                .andExpect(jsonPath("$[0].averageTime").value(10.5))
                .andExpect(jsonPath("$[1].serviceTypeName").value("ServiceType2"))
                .andExpect(jsonPath("$[1].averageTime").value(20.0));
    }

    @Test
    void whenGettingAverageExecutionTimeThrowsException_thenReturns500() throws Exception {
        when(getAverageExecutionTimeHandler.handle(any(GetAverageExecutionTimeRequest.class)))
                .thenThrow(new RuntimeException("Test exception"));

        String requestJson = "{\"timeUnit\":\"MINUTES\"}";

        mockMvc.perform(post("/monitoring/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void whenGettingAverageExecutionTimeByIdWithValidRequest_thenReturns200() throws Exception {
        ServiceAverageTime mockAverage = new ServiceAverageTime("ServiceType1", 15.0);
        UUID testId = UUID.randomUUID();
        when(getAverageExecutionTimeByIdHandler.handle(testId, AverageTimeEnum.HOURS))
                .thenReturn(mockAverage);

        String requestJson = "{\"timeUnit\":\"HOURS\",\"id\":\"" + testId + "\"}";

        mockMvc.perform(post("/monitoring/by-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceTypeName").value("ServiceType1"))
                .andExpect(jsonPath("$.averageTime").value(15.0));
    }

    @Test
    void whenGettingAverageExecutionTimeByIdThrowsException_thenReturns500() throws Exception {
        UUID testId = UUID.randomUUID();
        when(getAverageExecutionTimeByIdHandler.handle(testId, AverageTimeEnum.SECONDS))
                .thenThrow(new RuntimeException("Test exception"));

        String requestJson = "{\"timeUnit\":\"SECONDS\",\"id\":\"" + testId + "\"}";

        mockMvc.perform(post("/monitoring/by-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError());
    }
}
