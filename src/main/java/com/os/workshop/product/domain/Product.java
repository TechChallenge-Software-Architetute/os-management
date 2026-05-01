package com.os.workshop.product.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Product {

    protected Long id;
    protected String name;
    protected String sku;
    protected ProductType type;
    protected UnitOfMeasure unit;
    protected String category;
    protected String brand;
    protected BigDecimal costPrice;
    protected BigDecimal salePrice;
    protected boolean active = true;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
}
