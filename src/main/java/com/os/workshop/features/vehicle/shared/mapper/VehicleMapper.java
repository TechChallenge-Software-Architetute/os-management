package com.os.workshop.features.vehicle.shared.mapper;

import com.os.workshop.infrastructure.persistence.client.ClientEntity;
import com.os.workshop.features.vehicle.shared.domain.Vehicle;
import com.os.workshop.features.vehicle.shared.repository.VehicleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "plate",  source = "vehicle.plate.value")
    @Mapping(target = "client", source = "client")
    @Mapping(target = "brand",  source = "vehicle.brand")
    @Mapping(target = "model",  source = "vehicle.model")
    @Mapping(target = "year",   source = "vehicle.year")
    @Mapping(target = "color",  source = "vehicle.color")
    @Mapping(target = "type",   source = "vehicle.type")
    @Mapping(target = "active", source = "vehicle.active")
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    VehicleEntity toEntity(Vehicle vehicle, ClientEntity client);

    @Mapping(target = "plate",      source = "plate.value")
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "client",     ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    void updateEntity(@MappingTarget VehicleEntity entity, Vehicle vehicle);

    default Vehicle toDomain(VehicleEntity entity) {
        return Vehicle.reconstitute(
                entity.getId(),
                entity.getClient().getId(),
                entity.getPlate(),
                entity.getBrand(),
                entity.getModel(),
                entity.getYear(),
                entity.getColor(),
                entity.getType(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
