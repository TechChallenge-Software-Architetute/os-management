package com.os.workshop.service.domain.requests;

import com.os.workshop.utils.annotations.UpperCase;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateServiceRequest {
    @UpperCase
    private String ServiceType;

    private UUID idOS;
}