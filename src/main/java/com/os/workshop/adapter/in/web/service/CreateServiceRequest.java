package com.os.workshop.adapter.in.web.service;

import com.os.workshop.features.utils.annotations.UpperCase;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateServiceRequest {
    @UpperCase
    private String ServiceType;

    private UUID idOS;
}
