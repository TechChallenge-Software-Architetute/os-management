package com.os.features.user.persistence.mapper;

import com.os.features.user.domain.User;
import com.os.features.user.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, GroupMapper.class})
public interface UserMapper {
    User userEntityToUser(UserEntity userEntity);
    UserEntity userToUserEntity(User user);
}
