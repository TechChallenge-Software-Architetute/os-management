package com.os.workshop.infrastructure.persistence.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "supplies")
public class SupplyEntity extends ProductEntity {

    private boolean fractionalAllowed;
    private BigDecimal packageSize;
}
