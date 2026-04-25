package com.os.features.product.domain;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.AccessLevel;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Product {

    protected UUID id;
    protected String name;
    protected String sku;
    protected ProductType type;
    protected UnitOfMeasure unit;

    protected String category;
    protected String brand;

    protected BigDecimal costPrice;
    protected BigDecimal salePrice;

    protected boolean active;

}