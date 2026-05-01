package com.os.workshop.user.persistence.mapper;

import com.os.workshop.user.domain.Role;
import com.os.workshop.user.persistence.entity.RoleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toDomain(RoleEntity entity);
    RoleEntity toEntity(Role role);
}