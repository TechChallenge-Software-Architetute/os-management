package com.os.workshop.features.stock.shared.repository;

import com.os.workshop.features.stock.shared.domain.StockReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_reservations")
public class StockReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_reservation_seq")
    @SequenceGenerator(name = "stock_reservation_seq", sequenceName = "stock_reservation_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private Long stockId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private UUID serviceOrderId;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockReservationStatus status;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
