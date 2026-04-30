package com.os.workshop.service.domain;

import com.os.workshop.service.domain.enums.ServiceStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status {

    private ServiceStatusEnum status = ServiceStatusEnum.TO_DO;

    private LocalDateTime changedAt;
}
