package com.os.workshop.adapter.in.web.client;

import com.os.workshop.domain.serviceorder.Decision;
import jakarta.validation.constraints.NotNull;

public record DecisionRequest(
        @NotNull(message = "Decision is required (APPROVED or REJECTED)")
        Decision decision,
        String reason
) {}
