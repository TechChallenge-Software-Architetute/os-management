package com.os.workshop.adapter.in.web.monitoring;

import com.os.workshop.application.monitoring.GetAverageExecutionTimeByIdUseCase;
import com.os.workshop.application.monitoring.GetAverageExecutionTimeUseCase;
import com.os.workshop.domain.monitoring.ServiceAverageTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final GetAverageExecutionTimeUseCase getAverageExecutionTimeUseCase;
    private final GetAverageExecutionTimeByIdUseCase getAverageExecutionTimeByIdUseCase;

    @PostMapping("/all")
    public ResponseEntity<List<ServiceAverageTime>> getAverageExecutionTime(
            @RequestBody GetAverageExecutionTimeRequest request) {
        return ResponseEntity.ok(getAverageExecutionTimeUseCase.execute(request.getTimeUnit()));
    }

    @PostMapping("/by-id")
    public ResponseEntity<ServiceAverageTime> getAverageExecutionTimeById(
            @RequestBody GetAverageExecutionTimeByIdRequest request) {
        return ResponseEntity.ok(getAverageExecutionTimeByIdUseCase.execute(request.getId(), request.getTimeUnit()));
    }
}
