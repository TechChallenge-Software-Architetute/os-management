package com.os.workshop.features.user.persistence.mapper;

import com.os.workshop.features.user.domain.Group;
import com.os.workshop.features.user.persistence.entity.GroupEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    Group toDomain(GroupEntity entity);
    GroupEntity toEntity(Group group);
}
