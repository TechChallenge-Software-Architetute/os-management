package com.os.workshop.infrastructure.persistence.stock;

import com.os.workshop.domain.stock.StockMovementType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "stock_movements")
public class StockMovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_movement_seq")
    @SequenceGenerator(name = "stock_movement_seq", sequenceName = "stock_movement_seq", allocationSize = 50)
    private Long id;
    @Column(nullable = false) private Long stockId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private StockMovementType type;
    @Column(nullable = false) private BigDecimal quantity;
    private String reason;
    @Column(updatable = false) private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { this.createdAt = LocalDateTime.now(); }
}
