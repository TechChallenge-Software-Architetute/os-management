package com.os.workshop.features.user.shared.mapper;

import com.os.workshop.features.user.shared.domain.Role;
import com.os.workshop.features.user.shared.repository.RoleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toDomain(RoleEntity entity);
    RoleEntity toEntity(Role role);
}
