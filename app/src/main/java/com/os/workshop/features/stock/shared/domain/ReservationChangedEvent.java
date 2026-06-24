package com.os.workshop.features.stock.shared.domain;

import java.util.UUID;

/**
 * Event published whenever stock reservations change for a service order.
 * Listeners can react to recalculate budgets or trigger other side effects.
 */
public record ReservationChangedEvent(UUID serviceOrderId) {}
