package com.os.workshop.user.persistence.adapter;

import com.os.workshop.user.domain.Role;
import com.os.workshop.user.persistence.mapper.RoleMapper;
import com.os.workshop.user.persistence.repository.JpaRoleRepository;
import com.os.workshop.user.repository.RoleRepository;
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
