package com.os.workshop.domain.serviceorder;

import java.util.UUID;

public record OrderRejectedEvent(UUID serviceOrderId) {}
