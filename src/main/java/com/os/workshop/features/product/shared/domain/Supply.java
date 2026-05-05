package com.os.workshop.features.product.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supply extends Product {

    private boolean fractionalAllowed;
    private BigDecimal packageSize;
}
