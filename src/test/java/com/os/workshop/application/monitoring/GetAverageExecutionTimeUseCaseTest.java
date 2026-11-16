package com.os.workshop.application.monitoring;

import com.os.workshop.application.service.port.out.ServiceRepository;
import com.os.workshop.domain.monitoring.AverageTimeEnum;
import com.os.workshop.domain.monitoring.ServiceAverageTime;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAverageExecutionTimeUseCaseTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 8, 10, 0);

    @Mock private ServiceRepository serviceRepository;
    @InjectMocks private GetAverageExecutionTimeUseCase useCase;

    @Test
    void calculaAMediaPorTipoSoComServicosConcluidos() {
        when(serviceRepository.findAll()).thenReturn(List.of(
                done("TROCA_OLEO", 10),
                done("TROCA_OLEO", 20),
                notDone("TROCA_OLEO"),
                done("ALINHAMENTO", 30),
                doneBeforeDoing("BALANCEAMENTO"),
                notDone(null)));

        Map<String, Double> averages = useCase.execute(AverageTimeEnum.MINUTES).stream()
                .collect(Collectors.toMap(ServiceAverageTime::getServiceTypeName, ServiceAverageTime::getAverageTime));

        assertThat(averages).containsEntry("TROCA_OLEO", 15.0)
                .containsEntry("ALINHAMENTO", 30.0)
                .containsEntry("BALANCEAMENTO", 0.0)
                .containsEntry("Unknown", 0.0);
    }

    @Test
    void converteParaSegundosEHoras() {
        when(serviceRepository.findAll()).thenReturn(List.of(done("TROCA_OLEO", 90)));

        assertThat(useCase.execute(AverageTimeEnum.SECONDS).get(0).getAverageTime()).isEqualTo(5400.0);
        assertThat(useCase.execute(AverageTimeEnum.HOURS).get(0).getAverageTime()).isEqualTo(1.5);
    }

    @Test
    void semServicosRetornaListaVazia() {
        when(serviceRepository.findAll()).thenReturn(List.of());

        assertThat(useCase.execute(AverageTimeEnum.MINUTES)).isEmpty();
    }

    static WorkshopService done(String type, int minutes) {
        return new WorkshopService(UUID.randomUUID(), type, UUID.randomUUID(), List.of(
                new Status(ServiceStatusEnum.TO_DO, START.minusMinutes(5)),
                new Status(ServiceStatusEnum.DOING, START),
                new Status(ServiceStatusEnum.DONE, START.plusMinutes(minutes))));
    }

    static WorkshopService notDone(String type) {
        return new WorkshopService(UUID.randomUUID(), type, UUID.randomUUID(), List.of(
                new Status(ServiceStatusEnum.TO_DO, START),
                new Status(ServiceStatusEnum.DOING, START.plusMinutes(1))));
    }

    static WorkshopService doneBeforeDoing(String type) {
        return new WorkshopService(UUID.randomUUID(), type, UUID.randomUUID(), List.of(
                new Status(ServiceStatusEnum.DONE, START),
                new Status(ServiceStatusEnum.DOING, START.plusMinutes(10))));
    }
}
