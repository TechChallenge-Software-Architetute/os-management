package com.os.workshop.features.user.persistence.mapper;

import com.os.workshop.features.user.domain.Role;
import com.os.workshop.features.user.persistence.entity.RoleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toDomain(RoleEntity entity);
    RoleEntity toEntity(Role role);
}