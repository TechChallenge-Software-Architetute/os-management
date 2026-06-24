package com.os.workshop.features.user.shared.mapper;

import com.os.workshop.features.user.shared.domain.Group;
import com.os.workshop.features.user.shared.repository.GroupEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    Group toDomain(GroupEntity entity);
    GroupEntity toEntity(Group group);
}
