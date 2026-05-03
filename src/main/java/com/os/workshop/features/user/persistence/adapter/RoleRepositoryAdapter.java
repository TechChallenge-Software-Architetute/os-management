package com.os.workshop.features.user.persistence.adapter;

import com.os.workshop.features.user.domain.Role;
import com.os.workshop.features.user.persistence.mapper.RoleMapper;
import com.os.workshop.features.user.persistence.repository.JpaRoleRepository;
import com.os.workshop.features.user.repository.RoleRepository;
import org.springframework.stereotype.Component;

@Component
public class RoleRepositoryAdapter implements RoleRepository {

    private final JpaRoleRepository jpaRoleRepository;
    private final RoleMapper roleMapper;

    public RoleRepositoryAdapter(JpaRoleRepository jpaRoleRepository, RoleMapper roleMapper) {
        this.jpaRoleRepository = jpaRoleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public Role findByRoleName(String roleName) {
        return roleMapper.toDomain(jpaRoleRepository.findByName(roleName));
    }
}
