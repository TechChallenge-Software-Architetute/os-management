package com.os.workshop.features.service.domain.requests;

import com.os.workshop.features.utils.annotations.UpperCase;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateServiceRequest {
    @UpperCase
    private String serviceType;

    private UUID idOS;
}
