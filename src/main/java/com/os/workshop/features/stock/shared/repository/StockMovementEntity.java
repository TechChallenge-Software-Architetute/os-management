package com.os.workshop.features.stock.shared.repository;

import com.os.workshop.features.stock.shared.domain.StockMovementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_movements")
public class StockMovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_movement_seq")
    @SequenceGenerator(name = "stock_movement_seq", sequenceName = "stock_movement_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false)
    private Long stockId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockMovementType type;

    @Column(nullable = false)
    private BigDecimal quantity;

    private String reason;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
