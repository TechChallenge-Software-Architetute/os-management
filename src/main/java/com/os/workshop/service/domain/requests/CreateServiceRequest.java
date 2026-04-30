package com.os.workshop.service.domain.requests;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateServiceRequest {
    private String ServiceType;
    private UUID idOS;
}