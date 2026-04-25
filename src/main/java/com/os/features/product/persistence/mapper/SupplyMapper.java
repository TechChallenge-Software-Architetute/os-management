package com.os.features.product.persistence.mapper;

import com.os.features.product.domain.Supply;
import com.os.features.product.persistence.entity.SupplyEntity;

public final class SupplyMapper {

    private SupplyMapper() {}

    public static Supply toDomain(SupplyEntity entity) {
        Supply supply = new Supply();
        supply.setId(entity.getId());
        supply.setName(entity.getName());
        supply.setSku(entity.getSku());
        supply.setType(entity.getType());
        supply.setUnit(entity.getUnit());
        supply.setCategory(entity.getCategory());
        supply.setBrand(entity.getBrand());
        supply.setCostPrice(entity.getCostPrice());
        supply.setSalePrice(entity.getSalePrice());
        supply.setActive(entity.isActive());
        supply.setCreatedAt(entity.getCreatedAt());
        supply.setUpdatedAt(entity.getUpdatedAt());
        supply.setFractionalAllowed(entity.isFractionalAllowed());
        supply.setPackageSize(entity.getPackageSize());
        return supply;
    }

    public static SupplyEntity toEntity(Supply supply) {
        SupplyEntity entity = new SupplyEntity();
        entity.setName(supply.getName());
        entity.setSku(supply.getSku());
        entity.setType(supply.getType());
        entity.setUnit(supply.getUnit());
        entity.setCategory(supply.getCategory());
        entity.setBrand(supply.getBrand());
        entity.setCostPrice(supply.getCostPrice());
        entity.setSalePrice(supply.getSalePrice());
        entity.setActive(supply.isActive());
        entity.setFractionalAllowed(supply.isFractionalAllowed());
        entity.setPackageSize(supply.getPackageSize());
        return entity;
    }

    public static void updateEntity(SupplyEntity entity, Supply supply) {
        entity.setName(supply.getName());
        entity.setSku(supply.getSku());
        entity.setUnit(supply.getUnit());
        entity.setCategory(supply.getCategory());
        entity.setBrand(supply.getBrand());
        entity.setCostPrice(supply.getCostPrice());
        entity.setSalePrice(supply.getSalePrice());
        entity.setActive(supply.isActive());
        entity.setFractionalAllowed(supply.isFractionalAllowed());
        entity.setPackageSize(supply.getPackageSize());
    }
}
