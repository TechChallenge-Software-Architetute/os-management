package com.os.workshop.infrastructure.persistence.stock;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "stocks")
public class StockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_seq")
    @SequenceGenerator(name = "stock_seq", sequenceName = "stock_seq", allocationSize = 50)
    private Long id;
    @Column(nullable = false, unique = true) private Long productId;
    @Column(nullable = false) private BigDecimal quantity = BigDecimal.ZERO;
    @Column(nullable = false) private BigDecimal reservedQuantity = BigDecimal.ZERO;
    @Column(nullable = false) private BigDecimal minimumQuantity = BigDecimal.ZERO;
    @Column(updatable = false) private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @PrePersist protected void onCreate() { this.createdAt = LocalDateTime.now(); this.updatedAt = this.createdAt; }
    @PreUpdate protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
