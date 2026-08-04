package com.os.workshop.domain.stock;

import java.util.UUID;

public record ReservationChangedEvent(UUID serviceOrderId) {}
