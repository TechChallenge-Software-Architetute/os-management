package com.os.workshop.application.monitoring;

import com.os.workshop.application.service.port.out.ServiceRepository;
import com.os.workshop.domain.monitoring.AverageTimeEnum;
import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.Status;
import com.os.workshop.domain.service.WorkshopService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.os.workshop.application.monitoring.GetAverageExecutionTimeUseCaseTest.done;
import static com.os.workshop.application.monitoring.GetAverageExecutionTimeUseCaseTest.doneBeforeDoing;
import static com.os.workshop.application.monitoring.GetAverageExecutionTimeUseCaseTest.notDone;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAverageExecutionTimeByIdUseCaseTest {

    @Mock private ServiceRepository serviceRepository;
    @InjectMocks private GetAverageExecutionTimeByIdUseCase useCase;

    private final UUID serviceId = UUID.randomUUID();

    @Test
    void calculaOTempoDeUmServicoConcluido() {
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(done("TROCA_OLEO", 120)));

        var result = useCase.execute(serviceId, AverageTimeEnum.HOURS);

        assertThat(result.getServiceTypeName()).isEqualTo("TROCA_OLEO");
        assertThat(result.getAverageTime()).isEqualTo(2.0);
    }

    @Test
    void servicoNaoConcluidoTemTempoZero() {
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(notDone("TROCA_OLEO")));

        assertThat(useCase.execute(serviceId, AverageTimeEnum.MINUTES).getAverageTime()).isZero();
    }

    @Test
    void conclusaoAntesDoInicioTemTempoZero() {
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(doneBeforeDoing("TROCA_OLEO")));

        assertThat(useCase.execute(serviceId, AverageTimeEnum.MINUTES).getAverageTime()).isZero();
    }

    @Test
    void concluidoSemInicioTemTempoZero() {
        var service = new WorkshopService(serviceId, "TROCA_OLEO", UUID.randomUUID(),
                List.of(new Status(ServiceStatusEnum.DONE, LocalDateTime.now())));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        assertThat(useCase.execute(serviceId, AverageTimeEnum.SECONDS).getAverageTime()).isZero();
    }

    @Test
    void servicoSemTipoRetornaUnknown() {
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(notDone(null)));

        var result = useCase.execute(serviceId, AverageTimeEnum.MINUTES);

        assertThat(result.getServiceTypeName()).isEqualTo("Unknown");
        assertThat(result.getAverageTime()).isZero();
    }

    @Test
    void servicoInexistenteFalha() {
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(serviceId, AverageTimeEnum.MINUTES))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(serviceId.toString());
    }
}
