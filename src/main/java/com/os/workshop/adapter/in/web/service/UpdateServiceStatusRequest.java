package com.os.workshop.adapter.in.web.service;

import com.os.workshop.domain.service.ServiceStatusEnum;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateServiceStatusRequest {
    private ServiceStatusEnum status;
    private UUID id;
}
