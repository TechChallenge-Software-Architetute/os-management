package com.os.workshop.adapter.in.web.service;

import com.os.workshop.domain.service.ServiceType;

import java.util.UUID;

public record ServiceTypeResponse(UUID id, String name, String description) {
    public static ServiceTypeResponse from(ServiceType serviceType) {
        return new ServiceTypeResponse(serviceType.getId(), serviceType.getName(), serviceType.getDescription());
    }
}
