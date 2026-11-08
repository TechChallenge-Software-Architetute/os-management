package com.os.workshop.adapter.in.web.monitoring;

import com.os.workshop.application.monitoring.GetAverageExecutionTimeByIdUseCase;
import com.os.workshop.application.monitoring.GetAverageExecutionTimeUseCase;
import com.os.workshop.domain.monitoring.AverageTimeEnum;
import com.os.workshop.domain.monitoring.ServiceAverageTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitoringControllerTest {

    @Mock private GetAverageExecutionTimeUseCase getAverageExecutionTimeUseCase;
    @Mock private GetAverageExecutionTimeByIdUseCase getAverageExecutionTimeByIdUseCase;
    @InjectMocks private MonitoringController controller;

    @Test
    void retornaAMediaDeTodosOsTipos() {
        when(getAverageExecutionTimeUseCase.execute(AverageTimeEnum.MINUTES))
                .thenReturn(List.of(new ServiceAverageTime("TROCA_OLEO", 15.0)));
        var request = new GetAverageExecutionTimeRequest();
        request.setTimeUnit(AverageTimeEnum.MINUTES);

        var response = controller.getAverageExecutionTime(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(ServiceAverageTime::getServiceTypeName).containsExactly("TROCA_OLEO");
    }

    @Test
    void retornaATempoDeUmServico() {
        var serviceId = UUID.randomUUID();
        when(getAverageExecutionTimeByIdUseCase.execute(serviceId, AverageTimeEnum.HOURS))
                .thenReturn(new ServiceAverageTime("ALINHAMENTO", 2.0));
        var request = new GetAverageExecutionTimeByIdRequest();
        request.setId(serviceId);
        request.setTimeUnit(AverageTimeEnum.HOURS);

        var response = controller.getAverageExecutionTimeById(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAverageTime()).isEqualTo(2.0);
    }
}
