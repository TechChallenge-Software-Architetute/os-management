package com.os.features.product.domain;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supply extends Product {

    private boolean fractionalAllowed; // pode usar fração do insumo (ex: 3.5L)
    private BigDecimal packageSize;

}
