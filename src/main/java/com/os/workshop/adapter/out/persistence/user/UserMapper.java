package com.os.workshop.adapter.out.persistence.user;

import com.os.workshop.domain.user.Group;
import com.os.workshop.domain.user.Role;
import com.os.workshop.domain.user.User;
import com.os.workshop.infrastructure.persistence.user.GroupEntity;
import com.os.workshop.infrastructure.persistence.user.RoleEntity;
import com.os.workshop.infrastructure.persistence.user.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toDomain(UserEntity entity);
    Role toDomain(RoleEntity entity);
    Group toDomain(GroupEntity entity);
}
