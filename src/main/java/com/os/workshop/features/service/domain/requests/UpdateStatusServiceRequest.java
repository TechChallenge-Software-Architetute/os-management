package com.os.workshop.features.service.domain.requests;

import com.os.workshop.features.service.domain.enums.ServiceStatusEnum;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateStatusServiceRequest {
    private ServiceStatusEnum status;
    private UUID id;
}
