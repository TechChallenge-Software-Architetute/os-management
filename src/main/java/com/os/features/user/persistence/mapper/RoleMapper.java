package com.os.features.user.persistence.mapper;

import com.os.features.user.domain.Role;
import com.os.features.user.persistence.entity.RoleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toDomain(RoleEntity entity);
    RoleEntity toEntity(Role role);
}