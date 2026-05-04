package com.os.workshop.features.user.shared.repository;

import com.os.workshop.features.user.shared.domain.Role;
import com.os.workshop.features.user.shared.mapper.RoleMapper;
import org.springframework.stereotype.Component;

@Component
public class RolePersistenceAdapter implements RoleRepository {

    private final JpaRoleRepository jpaRoleRepository;
    private final RoleMapper roleMapper;

    public RolePersistenceAdapter(JpaRoleRepository jpaRoleRepository, RoleMapper roleMapper) {
        this.jpaRoleRepository = jpaRoleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public Role findByRoleName(String roleName) {
        return roleMapper.toDomain(jpaRoleRepository.findByName(roleName));
    }
}
