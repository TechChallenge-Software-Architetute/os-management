package com.os.features.user.persistence.mapper;

import com.os.features.user.domain.Group;
import com.os.features.user.persistence.entity.GroupEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    Group toDomain(GroupEntity entity);
    GroupEntity toEntity(Group group);
}
