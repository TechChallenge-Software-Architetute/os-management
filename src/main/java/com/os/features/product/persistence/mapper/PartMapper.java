package com.os.features.product.persistence.mapper;

import com.os.features.product.domain.Part;
import com.os.features.product.persistence.entity.PartEntity;

public final class PartMapper {

    private PartMapper() {}

    public static Part toDomain(PartEntity entity) {
        Part part = new Part();
        part.setId(entity.getId());
        part.setName(entity.getName());
        part.setSku(entity.getSku());
        part.setType(entity.getType());
        part.setUnit(entity.getUnit());
        part.setCategory(entity.getCategory());
        part.setBrand(entity.getBrand());
        part.setCostPrice(entity.getCostPrice());
        part.setSalePrice(entity.getSalePrice());
        part.setActive(entity.isActive());
        part.setCreatedAt(entity.getCreatedAt());
        part.setUpdatedAt(entity.getUpdatedAt());
        part.setManufacturerCode(entity.getManufacturerCode());
        part.setWarrantyMonths(entity.getWarrantyMonths());
        return part;
    }

    public static PartEntity toEntity(Part part) {
        PartEntity entity = new PartEntity();
        entity.setName(part.getName());
        entity.setSku(part.getSku());
        entity.setType(part.getType());
        entity.setUnit(part.getUnit());
        entity.setCategory(part.getCategory());
        entity.setBrand(part.getBrand());
        entity.setCostPrice(part.getCostPrice());
        entity.setSalePrice(part.getSalePrice());
        entity.setActive(part.isActive());
        entity.setManufacturerCode(part.getManufacturerCode());
        entity.setWarrantyMonths(part.getWarrantyMonths());
        return entity;
    }

    public static void updateEntity(PartEntity entity, Part part) {
        entity.setName(part.getName());
        entity.setSku(part.getSku());
        entity.setUnit(part.getUnit());
        entity.setCategory(part.getCategory());
        entity.setBrand(part.getBrand());
        entity.setCostPrice(part.getCostPrice());
        entity.setSalePrice(part.getSalePrice());
        entity.setActive(part.isActive());
        entity.setManufacturerCode(part.getManufacturerCode());
        entity.setWarrantyMonths(part.getWarrantyMonths());
    }
}
