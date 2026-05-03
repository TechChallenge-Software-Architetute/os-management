package com.os.workshop.features.user.repository;

import com.os.workshop.features.user.domain.Role;

public interface RoleRepository {
    Role findByRoleName(String roleName);
}
