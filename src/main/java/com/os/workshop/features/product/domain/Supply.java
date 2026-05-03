package com.os.workshop.features.product.domain;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supply extends Product {

    private boolean fractionalAllowed;
    private BigDecimal packageSize;
}
