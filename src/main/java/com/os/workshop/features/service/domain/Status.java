package com.os.workshop.features.service.domain;

import com.os.workshop.features.service.domain.enums.ServiceStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Service status history entry.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status {

    @Schema(description = "Service status value.", example = "TO_DO")
    private ServiceStatusEnum status = ServiceStatusEnum.TO_DO;

    @Schema(description = "Date and time when the status changed.", example = "2026-05-03T12:30:00")
    private LocalDateTime changedAt;
}
