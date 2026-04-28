package com.os.workshop.service.domain.requests;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateServiceRequest {
    private String ServiceType;
    private UUID idOS;
}