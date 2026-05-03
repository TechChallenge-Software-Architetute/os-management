package com.os.workshop.user.persistence.mapper;

import com.os.workshop.features.user.domain.Group;
import com.os.workshop.features.user.domain.Role;
import com.os.workshop.features.user.domain.User;
import com.os.workshop.features.user.persistence.entity.GroupEntity;
import com.os.workshop.features.user.persistence.entity.RoleEntity;
import com.os.workshop.features.user.persistence.entity.UserEntity;
import com.os.workshop.features.user.persistence.mapper.GroupMapperImpl;
import com.os.workshop.features.user.persistence.mapper.RoleMapperImpl;
import com.os.workshop.features.user.persistence.mapper.UserMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserMapperTest {

    private final UserMapperImpl mapper = new UserMapperImpl();

    UserMapperTest() {
        ReflectionTestUtils.setField(mapper, "roleMapper", new RoleMapperImpl());
        ReflectionTestUtils.setField(mapper, "groupMapper", new GroupMapperImpl());
    }

    @Test
    void convertsNestedRolesAndGroups() {
        UserEntity entity = new UserEntity(
                UUID.randomUUID(),
                "a@b.com",
                "pwd",
                Set.of(new RoleEntity(UUID.randomUUID(), "ROLE_ADMIN")),
                Set.of(new GroupEntity(UUID.randomUUID(), "BACKOFFICE"))
        );

        User user = mapper.userEntityToUser(entity);
        UserEntity mappedEntity = mapper.userToUserEntity(
                new User(UUID.randomUUID(), "b@b.com", "pwd", Set.of(new Role("USER")), Set.of(new Group("OPS")))
        );

        assertEquals("a@b.com", user.getEmail());
        assertTrue(user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN")));
        assertTrue(mappedEntity.getGroups().stream().anyMatch(group -> group.getName().equals("OPS")));
    }
}
