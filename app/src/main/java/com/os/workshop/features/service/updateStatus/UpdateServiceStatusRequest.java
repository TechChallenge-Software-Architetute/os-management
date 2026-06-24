package com.os.workshop.features.service.updateStatus;

import com.os.workshop.features.service.shared.domain.ServiceStatusEnum;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateServiceStatusRequest {
    private ServiceStatusEnum status;
    private UUID id;
}
