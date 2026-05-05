package com.os.workshop.features.user.shared.mapper;

import com.os.workshop.features.user.shared.domain.User;
import com.os.workshop.features.user.shared.repository.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, GroupMapper.class})
public interface UserMapper {
    User userEntityToUser(UserEntity userEntity);
    UserEntity userToUserEntity(User user);
}
